// package main
package main

import (
	"context"
	"database/sql"
	"fmt"
	"github.com/dtm-labs/client/dtmcli"
	"github.com/dtm-labs/client/dtmcli/dtmimp"
	"github.com/dtm-labs/dtm/dtmutil"
	"github.com/gin-gonic/gin"
	"github.com/ryotaro612/jepsen-tutorial/xabank/internal"
	"net/http"
	"os"
	"strconv"
	"time"
)

func main() {
	var err error
	logger := internal.NewLogger(true)

	defer func() {
		if err != nil {
			logger.Error("error", "error", err)
			os.Exit(1)
		}
	}()
	config, err := internal.LoadConfig()
	if err != nil {
		return
	}

	r := gin.Default()

	r.POST("/transactions", dtmutil.WrapHandler2(func(c *gin.Context) any {

		return dtmcli.XaLocalTransaction(c.Request.URL.Query(), config.DBConf(), func(db *sql.DB, xa *dtmcli.Xa) error {
			var json internal.Transaction
			if err := c.ShouldBindJSON(&json); err != nil {
				c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
				return err
			}
			logger.Debug("inside xa local")
			_, err := dtmimp.DBExec(config.Driver, db,
				"update dtm_app.account set balance = balance + ? where user_id = ?", json.Amount, json.User)
			if err != nil {
				logger.Error("update", "error", err)
			}

			return err
		})
	}))

	db, err := sql.Open("mysql", config.DataSourceName())
	defer func() {
		err = db.Close()
	}()
	if err != nil {
		return
	}

	ctx := context.Background()
	r.GET("/accounts/:userId", func(c *gin.Context) {
		userId := c.Param("userId")
		ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
		defer cancel()
		var balance float64
		err := db.QueryRowContext(ctx, "select balance from dtm_app.account where user_id = ?", userId).Scan(&balance)
		if userId == "" {
			c.JSON(400, gin.H{
				"message": "userId is required",
			})
			return
		}
		if err == sql.ErrNoRows {
			c.JSON(404, gin.H{
				"userId":  userId,
				"message": "not found",
			})
			return
		}
		if err != nil {
			c.JSON(500, gin.H{
				"message": fmt.Sprintf("%#v", err),
			})
			return
		}

		c.JSON(200, gin.H{
			"userId":  userId,
			"balance": balance,
		})
	})

	r.Run(":" + strconv.Itoa(config.Port))
}

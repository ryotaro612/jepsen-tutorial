package main

import (
	"database/sql"
	"log/slog"
	"net/http"
	"os"
	"strconv"

	"github.com/dtm-labs/client/dtmcli"
	"github.com/dtm-labs/client/dtmcli/dtmimp"
	"github.com/dtm-labs/dtm/dtmutil"
	"github.com/gin-gonic/gin"
)

type Transaction struct {
	User   string
	Amount int
}

func main() {
	var err error
	logger := slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{
		Level: slog.LevelDebug,
	}))
	defer func() {
		if err != nil {
			logger.Error("error", "error", err)
			os.Exit(1)
		}
	}()

	r := gin.Default()
	// 			conf.User, conf.Password, host, conf.Port, conf.Db
	dbPort, err := strconv.Atoi(os.Getenv("DB_PORT"))
	if err != nil {
		return
	}

	busiConf := dtmcli.DBConf{
		Driver:   "mysql",
		Host:     os.Getenv("DB_HOST"),
		Port:     int64(dbPort),
		User:     "root",
		Password: "root",
		Db:       "dtm_app",
	}
	logger.Debug("busiConf", "busiConf", busiConf)
	r.POST("/transactions", dtmutil.WrapHandler2(func(c *gin.Context) any {
		var json Transaction
		return dtmcli.XaLocalTransaction(c.Request.URL.Query(), busiConf, func(db *sql.DB, xa *dtmcli.Xa) error {
			if err := c.ShouldBindJSON(&json); err != nil {
				c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
				return err
			}
			logger.Debug("inside xa local")
			_, err := dtmimp.DBExec(busiConf.Driver, db, "update dtm_app.account set balance = balance + ? where user_id = ?", json.Amount, json.User)
			logger.Error("db exec", "error", err)
			//return SagaAdjustBalance(db, TransInUID, reqFrom(c).Amount, reqFrom(c).TransInResult)
			return err
		})
	}))
	r.Run(":" + os.Getenv("PORT")) // 0.0.0.0:8080 でサーバーを立てます。
}

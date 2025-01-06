package main

import (
	"database/sql"
	"github.com/dtm-labs/client/dtmcli"
	"github.com/dtm-labs/client/dtmcli/dtmimp"
	"github.com/dtm-labs/dtm/dtmutil"
	"github.com/gin-gonic/gin"
)

func main() {
	r := gin.Default()
	r.GET("/ping", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "pong",
		})
	})
	// 			conf.User, conf.Password, host, conf.Port, conf.Db
	busiConf := dtmcli.DBConf{
		Driver: "mysql",
		Host:   "localhost",
		Port:   3306,
		User:   "root",
	}
	r.POST("/transactions", dtmutil.WrapHandler2(func(c *gin.Context) interface{} {
		return dtmcli.XaLocalTransaction(c.Request.URL.Query(), busiConf, func(db *sql.DB, xa *dtmcli.Xa) error {
			_, err := dtmimp.DBExec(busiConf.Driver, db, "update dtm_busi.user_account set balance = balance + ? where user_id = ?", 30, "userid")
			//return SagaAdjustBalance(db, TransInUID, reqFrom(c).Amount, reqFrom(c).TransInResult)
			return err
		})
	}))
	r.Run() // 0.0.0.0:8080 でサーバーを立てます。
}

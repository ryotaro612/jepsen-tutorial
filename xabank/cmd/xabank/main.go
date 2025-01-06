package main

import (
	"flag"
	"fmt"
	"log/slog"
	"os"
	"time"

	"github.com/dtm-labs/client/dtmcli"
	"github.com/go-resty/resty/v2"
)

var dtmServerURL string
var appURLs []string
var from int
var amount int

func init() {
	appURLs = make([]string, 2)
	flag.StringVar(&dtmServerURL, "dtm", "http://localhost:36789/api/dtmsvr", "Dtm server endpoint.")
	flag.StringVar(&appURLs[0], "app1", "http://localhost:8080", "App server endpoint.")
	flag.StringVar(&appURLs[1], "app2", "http://localhost:8081", "App server endpoint.")
	flag.IntVar(&from, "from", 0, "app")
	flag.IntVar(&amount, "amount", 30, "amount")
}

type Transaction struct {
	User   string
	Amount int
}

func main() {
	flag.Parse()
	logger := slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{
		Level: slog.LevelDebug,
	}))

	var to int
	var err error
	defer func() {
		if err != nil {
			logger.Error("main", "error", err)
			os.Exit(1)
		}
	}()
	if from == 0 {
		to = 1
	} else if from == 1 {
		to = 0
	} else {
		err = fmt.Errorf("%#v", from)
		return
	}
	logger.Info("konichiwa", "from", from, "to", to, "amount", amount)
	gid := dtmcli.MustGenGid(dtmServerURL)
	logger.Debug("issue gid", "gid", gid)
	err = dtmcli.XaGlobalTransaction(dtmServerURL, gid, func(xa *dtmcli.Xa) (*resty.Response, error) {
		user := "alice"
		logger.Debug("inside xa global")
		resp, err := xa.CallBranch(&Transaction{User: user, Amount: -amount}, appURLs[from]+"/transactions")
		logger.Debug("call branch", "error", err)
		if err != nil {
			return resp, err
		}
		return xa.CallBranch(&Transaction{User: user, Amount: amount}, appURLs[to]+"/transactions")
	})
	if err != nil {
		logger.Error("xa global transaction", "error", err)
	}
	time.Sleep(3 * time.Second)
}

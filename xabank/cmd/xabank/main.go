package main

import (
	"fmt"
	"github.com/dtm-labs/client/dtmcli"
	"github.com/go-resty/resty/v2"
	"os"
)

type Trans struct {
	User   string
	Amount int
}

func main() {
	dtmServerURL := "http://localhost:36789/api/dtmsvr"
	appServerURL := "http://localhost:8081"
	appServerURL2 := "http://localhost:8082"
	gid := dtmcli.MustGenGid(dtmServerURL)
	err := dtmcli.XaGlobalTransaction(dtmServerURL, gid, func(xa *dtmcli.Xa) (*resty.Response, error) {
		resp, err := xa.CallBranch(&Trans{User: "alice", Amount: 30}, appServerURL+"/transactions")
		if err != nil {
			return resp, err
		}
		return xa.CallBranch(&Trans{User: "alice", Amount: -30}, appServerURL2+"/transactions")
	})
	if err != nil {
		fmt.Errorf("err: %v", err)
		os.Exit(1)
	}
}

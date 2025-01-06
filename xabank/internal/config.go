package internal

import (
	"github.com/dtm-labs/client/dtmcli"
	"os"
	"strconv"
)

type Config struct {
	Port       int
	DBHost     string
	DBPort     int
	DBUser     string
	DBPassword string
	DBSchema   string
	Driver     string
}

// LoadConfig loads the configuration from the environment
func LoadConfig() (Config, error) {
	res := Config{
		DBUser:     "root",
		DBPassword: "root",
		DBSchema:   "dtm_app",
		Driver:     "mysql",
	}
	var err error
	res.DBPort, err = strconv.Atoi(os.Getenv("DB_PORT"))
	if err != nil {
		return res, err
	}
	res.Port, err = strconv.Atoi(os.Getenv("PORT"))
	if err != nil {
		return res, err
	}
	if err != nil {
		return res, err
	}
	res.DBHost = os.Getenv("DB_HOST")
	return res, nil
}

// DBConf returns the database configuration
func (c Config) DBConf() dtmcli.DBConf {
	return dtmcli.DBConf{
		Driver:   c.Driver,
		Host:     c.DBHost,
		Port:     int64(c.DBPort),
		User:     c.DBUser,
		Password: c.DBPassword,
		Db:       c.DBSchema,
	}
}

func (c Config) DBPortStr() string {
	return strconv.Itoa(c.DBPort)
}

func (c Config) DataSourceName() string {
	return c.DBUser + ":" + c.DBPassword + "@(" + c.DBHost + ":" + c.DBPortStr() + ")/" + c.DBSchema + "?charset=utf8mb4"
}

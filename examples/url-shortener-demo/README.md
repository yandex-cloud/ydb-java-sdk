# YDB URL Shortener Demo

This is a simple Web Server application which basically does two things:
* it accepts an URL and stores it in the `urls` YDB table along with the computed hash value;
* it redirects the requests with the hash value passed to the original URL, by locating the
  proper URL value in the `urls` table.

On the YDB side, the application demonstrates basic UPSERT + SELECT functionality.

HTTP support is provided with the embedded Jetty libraries.

To run this application, the following command line parameters are supported:
* `-p`, `--listen-port`: the port for the embedded HTTP server, default `9000`;
* `-e`, `--endpoint`, YDB endpoint to use in the `host:port` format, default `localhost:2136`;
* `-d`, `--database`, YDB database name, default `/local`;
* `-c`, `--cert`, Path to PEM certificate, for self-signed certificates;
* `-t`, `--tls`, Use TLS encrypted connection if this parameter is present.
* `-i`, `--iam`, Use YC IAM to authenticate with YDB if this parameter is present.

If the IAM mode is used, the authentication mode for YDB needs to be configured with
the environment variables, as defined in the SDK documentation:
https://ydb.tech/ru/docs/reference/ydb-sdk/auth#env

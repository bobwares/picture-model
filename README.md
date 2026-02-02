# Picture Model App

## Domain

* Image files
* Hard Drive attached to Computer
* Wifi Network
* Browser
* Http Server
* Browser App
* Database of image files.

## Goal

Crawl images saved on a hard drive and create records for each image.

Access the images through a Browser App.

Edit Meta Information about each image.

Display Images in the Browser App.

Provide a search of the images by file name, meta data.

## Drive Connections

Each drive must be connected before the crawler or image viewer can read files from it.
Connecting a drive creates a `FileSystemProvider`, verifies it can access the configured root path,
and caches it in memory for reuse.

What "connected" means depends on the drive type:

| Type  | What `connect()` does |
|-------|-----------------------|
| LOCAL | Checks that the root path exists and is a directory. No network or credentials involved. |
| SFTP  | Opens an SSH session and authenticates with the configured credentials. |
| FTP   | Opens an FTP session and logs in. |
| SMB   | Establishes a jCIFS session to the network share. |

Once connected, the provider is cached by drive ID. The crawler and the image/thumbnail
endpoints reuse the cached provider for all file reads — no re-authentication on each request.
If a cached provider goes stale (e.g. network drop), `ConnectionManager.getProvider()` will
reconnect automatically. A health check runs every 5 minutes and evicts any provider that
reports itself as no longer connected.

## Local Database (PostgreSQL)

Use Docker Compose profiles and the provided environment file to run PostgreSQL locally.

1. Copy `.env.example` to `.env.postgresql` if you want custom values.
2. Start the database:
   - `make db-up`
3. Apply migrations and seed data:
   - `make db-load`
4. Stop the database:
   - `make db-down`

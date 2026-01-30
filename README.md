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

## Local Database (PostgreSQL)

Use Docker Compose profiles and the provided environment file to run PostgreSQL locally.

1. Copy `.env.example` to `.env.postgresql` if you want custom values.
2. Start the database:
   - `make db-up`
3. Apply migrations and seed data:
   - `make db-load`
4. Stop the database:
   - `make db-down`

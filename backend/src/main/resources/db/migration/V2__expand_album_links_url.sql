-- Increase album_links.url column size from 512 to 2048 characters
-- to accommodate longer streaming platform URLs.

ALTER TABLE album_links
    ALTER COLUMN url TYPE VARCHAR(2048);
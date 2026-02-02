# Static Map Providers (Personal Use, Low Volume)

Context
- Goal: render static map images for photo GPS coordinates.
- Usage: personal, static maps, ~100 requests/month.

Recommended options
1) Thunderforest (Hobby/free tier)
- Best fit for low-volume static map images.
- Static Map API requests count as 10 tile requests each.
- Free hobby plan has a high monthly tile limit, so 100 static maps/month is well within limits.
- Requires API key and attribution.
- Notes: read their terms and attribution requirements before use.

2) OSM US Tileservice (free, non-commercial)
- Tile server only (no static map endpoint).
- You would need to render tiles into a static image yourself.
- Requires attribution and compliance with their usage policy.
- Suitable if you want a purely OSM-based stack with no paid tier.

3) Stadia Maps (paid for static maps)
- Static Maps requires a paid plan.
- Free tier is non-commercial and does not include static maps.
- Good option if you later need higher volume or commercial use.

Implementation notes (future)
- Prefer a provider with a static map endpoint to keep backend simple.
- Store provider choice and API key in server config (not in UI).
- Always include proper map attribution in the UI.

Decision placeholder
- Provider: TBD (likely Thunderforest for simplicity and low volume).
- Attribution placement: image detail sidebar or below map image.

References
- Thunderforest pricing page
- OSM US Tileservice usage policy
- Stadia Maps pricing page

Last updated: 2026-02-01

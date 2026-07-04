# GCS Static Hosting — REMOVED
#
# Portal and Demo frontends have been migrated to Cloudflare Pages.
# The GCS buckets and IAM bindings previously defined here have been removed.
#
# Before running `terraform apply`, manually remove the old state entries:
#   terraform state rm google_storage_bucket.demo_app
#   terraform state rm google_storage_bucket.portal_app
#   terraform state rm google_storage_bucket_iam_member.demo_app_viewer
#   terraform state rm google_storage_bucket_iam_member.portal_app_viewer

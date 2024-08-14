#!/bin/sh

# from the dropdown at the top of Cloud Console:
export GCLOUD_PROJECT="fiery-blade-407922" 
# from Step 2.2 above:
export REPO="undl-web-scraper"
# the region you chose in Step 2.4:
export REGION="us-east4"
# whatever you want to call this image:
export IMAGE="unsc-voting-records-sync"
export INSTANCE_NAME="unsc-voting-records-sync"

# use the region you chose above here in the URL:
export IMAGE_TAG=${REGION}-docker.pkg.dev/$GCLOUD_PROJECT/$REPO/$IMAGE

# Build the image:
docker build -t $IMAGE_TAG --platform linux/amd64 .
# # Push it to Artifact Registry:
docker push $IMAGE_TAG
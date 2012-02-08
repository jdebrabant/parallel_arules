#!/bin/bash

# script to launch a keep-alive cluster in Amazon AWS. 
# usage: ./launch_cluster.sh <cluster size>

elastic-mapreduce \
  --create \
  --alive \
  --name "pfp-test-cluster" \
  --log-uri s3n://kdd12/logs/pfp/ \
  --num-instances 2 \
  --instance-type m2.2xlarge \
  --master-instance-type m2.2xlarge \
  --bootstrap-action s3://elasticmapreduce/bootstrap-actions/configurations/latest/memory-intensive 

#!/bin/bash

# script to launch a keep-alive cluster in Amazon AWS. 
# usage: ./launch_cluster.sh <cluster size>

elastic-mapreduce \
  --create \
  --alive \
  --name "parmm" \
  --log-uri s3n://kdd12/logs/ \
  --num-instances $1 \
  --instance-type m1.xlarge \
  --master-instance-type m1.xlarge \
  --bootstrap-action s3://elasticmapreduce/bootstrap-actions/configurations/latest/memory-intensive 

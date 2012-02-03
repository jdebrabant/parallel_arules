#!/bin/bash

elastic-mapreduce \
  --create \
  --name "test" \
  --jar s3n://kdd12/parallel.jar \
  --arg .02 \
  --arg .02 \
  --arg 20 \
  --arg 10 \
  --arg 100000 \
  --arg 64 \
  --arg 0.1 \
  --arg 4 \
  --arg s3n://kdd12/input/ \
  --arg s3n://kdd12/output/output1/ \
  --arg s3n://kdd12/output/output2/ \
  --log-uri s3n://kdd12/logs/ \
  --num-instances 2 \
  --instance-type m1.xlarge \
  --master-instance-type m1.xlarge \
  --bootstrap-action s3://elasticmapreduce/bootstrap-actions/configurations/latest/memory-intensive 

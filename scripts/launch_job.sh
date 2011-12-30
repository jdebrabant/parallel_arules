#!/bin/bash

elastic-mapreduce \
  --create \
  --jar s3n://kdd12/parallel.jar \
  --arg s3n://kdd12/output/output2/ \
  --arg s3n://kdd12/output/output1/ \
  --num-instances 2 \
  --master-instance-type 

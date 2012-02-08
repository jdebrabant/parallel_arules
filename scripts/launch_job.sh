#!/bin/bash

elastic-mapreduce \
  --create \
  --name "parmm-speedup-8" \
  --jar s3n://kdd12/parallel.jar \
  --arg .05 \
  --arg .01 \
  --arg 10 \
  --arg 28 \
  --arg 20000000 \
  --arg 46 \
  --arg .04 \
  --arg 4 \
  --arg s3n://kdd12/input/input_seq/20M/ \
  --arg s3n://kdd12/output/parmm/speedup/8/output1/ \
  --arg s3n://kdd12/output/parmm/speedup/8/output2/ \
  --log-uri s3n://kdd12/logs/parmm/speedup/8/ \
  --num-instances 8 \
  --instance-type m2.2xlarge \
  --master-instance-type m2.2xlarge \
  --bootstrap-action s3://elasticmapreduce/bootstrap-actions/configurations/latest/memory-intensive \
  --bootstrap-action s3://elasticmapreduce/bootstrap-actions/configure-daemons \
  --arg --namenode-heap-size=4096

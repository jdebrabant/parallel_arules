#!/bin/bash

elastic-mapreduce \
  --create \
  --name "test" \
  --jar s3n://kdd12/parallel.jar \
  --arg .1 \                            # epsilon
  --arg .1 \                            # delta
  --arg .2 \                            # minimum frequency 
  --arg .1 \                            # d
  --arg 100000 \                         # number of transactions
  --arg 64 \                            # map output groups
  --arg 1 \                             # mapper id
  --arg s3n://kdd12/input/ \            # path to input database
  --arg s3n://kdd12/output/output1/ \   # path to output local FIs
  --arg s3n://kdd12/output/output2/ \   # path to output global FIs
  --log-uri s3n://kdd12/logs/ \
  --num-instances 2 \
  --instance-type m1.xlarge \
  --master-instance-type m1.xlarge \
  --bootstrap-action s3://elasticmapreduce/bootstrap-actions/configurations/latest/memory-intensive 


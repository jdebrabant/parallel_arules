#!/bin/bash

# launches a PFP job using a previously-launched job cluster
# usage: ./launch_pfp.sh <job number> <input> <output>

elastic-mapreduce \
    --create \
    --name "pfp" \
    --jar s3n://kdd12/mahout.jar \
    --arg org.apache.mahout.fpm.pfpgrowth.FPGrowthDriver \
    --arg -i \
    --arg s3n://kdd12/input/input_dat/5M \
    --arg -o \
    --arg s3n://kdd12/output/pfp/5M \
    --arg -k \
    --arg 1000 \
    --arg -method \
    --arg mapreduce \
    --arg -s \
    --arg 2 \
    --log-uri s3n://kdd12/logs/pfp/ \
    --num-instances 2 \
    --instance-type m2.2xlarge \
    --master-instance-type m2.2xlarge \
    --bootstrap-action s3://elasticmapreduce/bootstrap-actions/configurations/latest/memory-intensive 

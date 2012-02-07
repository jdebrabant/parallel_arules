#!/bin/bash

# launches a PFP job using a previously-launched job cluster
# usage: ./launch_pfp.sh <job number> <input> <output>

elastic-mapreduce \
    --j $1 \
    --jar s3n://kdd12/mahout.jar \
    --main-class org.apache.mahout.fpm.pfpgrowth.FPGrowthDriver \
    --arg -i \
    --arg $2 \
    --arg -o \
    --arg $3 \
    --arg -k \
    --arg 100 \
    --arg -method \
    --arg mapreduce \
    --arg -s \
    --arg 2 \


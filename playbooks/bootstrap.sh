#!/bin/bash

# SSH Key
eval `ssh-agent` &&  ssh-add ~/.ssh/Akka.pem 

# AWS Keys
export AWS_ACCESS_KEY_ID=AKIAJDRMV47X5KU6T6SA
export AWS_SECRET_ACCESS_KEY=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

# Source environment vars
source ~/ansible/hacking/env-setup
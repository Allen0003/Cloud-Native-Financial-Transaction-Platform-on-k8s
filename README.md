Cloud-Native Financial Transaction Platform on Kubernetes

A production-style cloud-native financial transaction system built on AWS EKS, designed with stateless microservices, resilient batch processing, CI/CD automation, and disaster simulation.

🧭 Project Vision

This project simulates a real-world financial transaction platform deployed on Kubernetes.

It is designed not as a demo application, but as a production-oriented system that focuses on:

High availability

Fault tolerance

Rolling deployments

Idempotent batch processing

Observability

Disaster resilience

The goal is to demonstrate practical cloud-native engineering capability, not just Kubernetes knowledge.

🏗 Architecture Overview

Cloud Provider: Amazon Web Services
Kubernetes: Amazon EKS
Backend: Spring Boot
Database: Amazon RDS (MySQL Primary + Replica)
CI/CD: GitHub Actions

Client
   ↓
K8s Service
   ↓
Transaction API (Stateless Pods)
   ↓
RDS MySQL (Primary / Replica)
   ↓
Batch Settlement Service

🚀 Core Features
1️⃣ Transaction API (Stateless Design)

Create transaction

Query transaction

Stateless microservice

Dockerized

Deployed on EKS

Readiness & Liveness Probes configured

Why Stateless?

Enables horizontal scaling

Safe rolling update

Pod failure is transparent to users

Demo scenario:

curl → API works
kill pod → API still works

2️⃣ Financial Batch Settlement System

Simulates real financial daily settlement.

Features:

Idempotent batch logic

Handles crash recovery

Single-writer guarantee

Connection pooling

Failure simulation included

Why Idempotent?

Financial systems cannot tolerate double counting.

If batch crashes:

Restart safely

No duplicate settlement

Consistent final state

3️⃣ CI/CD Pipeline

Automated deployment using GitHub Actions:

Build

Test

Docker image push

Deploy to EKS

Rolling update

Rollback strategy

Why Rolling Update?

Avoid full outage

Gradual traffic shift

Readiness probe blocks unhealthy version

4️⃣ Disaster Simulation (Chaos Testing)

This project intentionally simulates:

Pod kill

OOM scenario

DB timeout

Batch crash

Traffic stress

HPA scaling

The goal is to predict system behavior under failure.

5️⃣ Observability

Structured logging

Basic metrics

Health endpoint

Simple dashboard

Because production systems without observability are blind systems.

🔥 Engineering Decisions
Why not Stateful Pods?

Stateless service enables:

Safe horizontal scaling

Zero-downtime deploy

Fault tolerance

Why single-writer batch?

Prevent race condition and inconsistent settlement.

Why readiness probe matters?

Prevents bad version from receiving traffic during deployment.

📊 What This Project Demonstrates

Kubernetes workload design

Cloud-native architecture

Resilient financial batch processing

CI/CD automation

Failure recovery strategy

Production thinking

🛠 Tech Stack
| Layer     | Technology     |
| --------- | -------------- |
| Cloud     | AWS            |
| K8s       | EKS            |
| Backend   | Spring Boot    |
| Database  | RDS MySQL      |
| CI/CD     | GitHub Actions |
| Container | Docker         |

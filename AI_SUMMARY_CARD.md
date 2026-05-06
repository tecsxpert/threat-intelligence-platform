# AI Threat Intelligence Platform

## Project Overview

This project is a secure AI-powered threat intelligence platform designed to safely process user prompts while protecting against common web and AI-specific attacks.

The system integrates:
- React Frontend
- Spring Boot Backend
- Flask AI Service
- Groq LLM Integration

---

## Core Features

### 1. AI Prompt Processing
Users can submit prompts through the frontend and receive AI-generated responses.

### 2. Prompt Injection Protection
Malicious prompts such as:
- "Ignore previous instructions"
- "Reveal secrets"
- "Hack the system"

are automatically detected and blocked.

### 3. PII Detection
Sensitive inputs including:
- Phone numbers
- Emails
- Personal identifiers

are detected before processing.

### 4. Secure API Communication
Frontend, backend, and AI services communicate securely using:
- JWT authentication
- Input validation
- Sanitization
- Rate limiting
- CORS protection

---

## Application Architecture

Frontend (React + Vite)
↓
Backend API (Spring Boot)
↓
AI Service (Flask)
↓
Groq LLM API

---

## Endpoints

### Backend
- GET /ai/test
- POST /ai/prompt
- GET /

### AI Service
- POST /test
- POST /recommend
- GET /health

---

## Security Controls

- Prompt Injection Detection
- SQL Injection Protection
- PII Detection
- JWT Validation
- Rate Limiting
- Secure Error Handling
- Input Sanitization

---

## Technologies Used

### Backend
- Java
- Spring Boot
- Maven

### AI Service
- Python
- Flask
- Groq API

---

## GitHub Repository

https://github.com/tecsxpert/threat-intelligence-platform

---

## Demo Highlights

- AI response generation
- Prompt injection blocking
- PII detection
- Secure API integration
- End-to-end frontend/backend communication
# Fallback templates - returned when Groq API fails or times out
# These are beginner-friendly default responses

FALLBACK_DESCRIBE = {
    "description": "Fallback Response - Groq API temporarily unavailable",
    "type": "security_threat",
    "severity": "medium",
    "details": "Unable to analyze the threat details. Please try again later.",
    "recommendations": [
        "Check system logs for more information",
        "Contact security team if issue persists",
        "Review basic security guidelines in documentation"
    ]
}

FALLBACK_RECOMMENDATIONS = [
    {
        "action_type": "monitor",
        "description": "Fallback: Monitor system logs for suspicious activity",
        "priority": "high"
    },
    {
        "action_type": "alert",
        "description": "Fallback: Set up alerts for critical events",
        "priority": "medium"
    },
    {
        "action_type": "patch",
        "description": "Fallback: Apply latest security patches",
        "priority": "low"
    }
]

FALLBACK_REPORT = {
    "title": "Fallback Security Report",
    "summary": "Unable to generate detailed report - AI service temporarily unavailable",
    "threat_analysis": {
        "name": "Unknown Threat",
        "risk_level": "medium",
        "description": "Please try again later for detailed analysis"
    },
    "recommendations": FALLBACK_RECOMMENDATIONS,
    "generated_by": "fallback_system",
    "status": "incomplete"
}

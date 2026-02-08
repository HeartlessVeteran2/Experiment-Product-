# Security Policy

## Supported Versions

This project follows semantic versioning and maintains security support only for actively maintained branches. The table below lists which versions currently receive security updates. Versions not listed are considered end-of-life and will not receive security fixes.

| Version / Branch | Supported          | Support details                                                                 |
| ---------------- | ------------------ | ------------------------------------------------------------------------------- |
| `main`           | :white_check_mark: | Actively developed; receives all security fixes and new features.              |
| `v2.x`           | :white_check_mark: | Current stable major version; receives security and critical bug fixes.        |
| `v1.x`           | :white_check_mark: | Maintenance-only; receives critical security fixes until 2025-12-31.           |
| `< v1.0`         | :x:                | End-of-life; no further updates, including security fixes.                     |

## Reporting a Vulnerability

If you believe you have found a security vulnerability in this project, please report it **privately** using one of the following channels:

- **GitHub Security Advisories (preferred)**:  
  1. Navigate to this repository on GitHub.  
  2. Open the **Security** tab.  
  3. Click **"Report a vulnerability"** and follow the instructions to submit a private security advisory.

If you are unable to use GitHub Security Advisories for any reason, please open a minimal issue indicating that you have a security report and prefer to share details privately. Do **not** include any sensitive technical details in that public issue; maintainers will provide a private contact method for further communication.

### What to Expect

- **Acknowledgment:** We aim to acknowledge receipt of your report within **5 business days**.
- **Initial assessment:** We will perform an initial triage and risk assessment, typically within **10 business days** of acknowledgment.
- **Status updates:** While the issue is being investigated and fixed, we will provide progress updates at least every **7 days** until resolution, or until we determine that the report is not a security issue.

### Disclosure and Remediation

- Valid vulnerabilities will be addressed in supported versions listed above. We will work to develop and test a fix, and prepare a release or patch as needed.
- Whenever appropriate, we may assign or request a **CVE identifier** for the issue.
- We will coordinate a **responsible disclosure** timeline with you. In general, we prefer to disclose details only **after** a fix or mitigation is available to users.
- If we conclude that a report does not represent a security vulnerability, we will explain our reasoning to you.

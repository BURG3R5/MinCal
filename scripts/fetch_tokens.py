import json
from google_auth_oauthlib.flow import InstalledAppFlow

SCOPES = [
    "https://www.googleapis.com/auth/calendar.calendarlist.readonly",
    "https://www.googleapis.com/auth/calendar.readonly",
    "https://www.googleapis.com/auth/calendar.events.readonly",
]

CLIENT_SECRET_FILE = "client_secret.json"


def main():
    flow = InstalledAppFlow.from_client_secrets_file(
        CLIENT_SECRET_FILE,
        scopes=SCOPES
    )

    credentials = flow.run_local_server(
        port=0,
        access_type="offline",
        prompt="consent"
    )

    token_data = {
        "access_token": credentials.token,
        "refresh_token": credentials.refresh_token,
        "client_id": credentials.client_id,
        "client_secret": credentials.client_secret,
    }

    with open("credentials.json", "w") as f:
        json.dump(token_data, f, indent=2)


if __name__ == "__main__":
    main()

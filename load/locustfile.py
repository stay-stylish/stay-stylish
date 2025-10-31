from locust import HttpUser, task, between

class StayStylishUser(HttpUser):
    wait_time = between(1, 2)

    @task
    def get_my_profile(self):
        self.client.get("/api/v1/users/me")
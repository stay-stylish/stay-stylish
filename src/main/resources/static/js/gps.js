document.getElementById("gpsBtn").addEventListener("click", () => {
    if (!navigator.geolocation) {
        alert("GPS를 지원하지 않는 브라우저입니다.");
        return;
    }

    navigator.geolocation.getCurrentPosition(success, error);
});

async function success(position) {
    const lat = position.coords.latitude;
    const lon = position.coords.longitude;

    document.getElementById("debug").textContent =
        `GPS 수신됨: 위도 ${lat}, 경도 ${lon}`;

    try {
        const res = await fetch("/api/v1/weather/weather-by-gps", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({latitude: lat, longitude: lon})
        });

        const data = await res.json();
        console.log("날씨 응답:", data);

        if (!data?.data) {
            alert("날씨 정보를 불러오지 못했습니다.");
            return;
        }

        const weather = data.data;

        document.getElementById("location").textContent =
            `${weather.province} ${weather.city} ${weather.district}`;
        document.getElementById("temperature").textContent = weather.temperature;
        document.getElementById("humidity").textContent = weather.humidity;
        document.getElementById("rainfall").textContent = weather.rainfall;
        document.getElementById("windSpeed").textContent = weather.windSpeed;
        document.getElementById("sky").textContent = weather.sky;
        document.getElementById("pty").textContent = weather.pty;

        document.getElementById("weather-card").classList.remove("hidden");

    } catch (e) {
        console.error(e);
        alert("서버 통신 오류");
    }
}

function error(err) {
    console.error(err);
    alert("GPS 권한을 허용해 주세요.");
}

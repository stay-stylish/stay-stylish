// 요소 미리 가져오기
const gpsBtn = document.getElementById("gpsBtn");
const debugEl = document.getElementById("debug");
const locationEl = document.getElementById("location");
const tempEl = document.getElementById("temperature");
const humidityEl = document.getElementById("humidity");
const rainfallEl = document.getElementById("rainfall");
const windEl = document.getElementById("windSpeed");
const skyEl = document.getElementById("sky");
const ptyEl = document.getElementById("pty");
const weatherCard = document.getElementById("weather-card");

// 버튼 클릭 이벤트
gpsBtn.addEventListener("click", () => {
    if (!navigator.geolocation) {
        alert("GPS를 지원하지 않는 브라우저입니다.");
        return;
    }

    getCurrentLocation()
        .then(fetchWeatherByGPS)
        .then(updateWeatherCard)
        .catch(handleError);
});

// GPS 위치 가져오기
function getCurrentLocation() {
    return new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
        });
    });
}

// 날씨 API 호출
async function fetchWeatherByGPS(position) {
    const { latitude, longitude } = position.coords;
    debugEl.textContent = `GPS 수신됨: 위도 ${latitude}, 경도 ${longitude}`;

    const token = localStorage.getItem("token");

    const res = await fetch("/api/v1/weather/weather-by-gps", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": token ? "Bearer " + token : ""
        },
        body: JSON.stringify({ latitude, longitude })
    });

    if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
    const data = await res.json();

    if (!data?.data) throw new Error("날씨 정보를 불러오지 못했습니다.");
    return data.data;
}

// DOM 업데이트
function updateWeatherCard(weather) {
    locationEl.textContent = `${weather.province} ${weather.city} ${weather.district}`;
    tempEl.textContent = weather.temperature;
    humidityEl.textContent = weather.humidity;
    rainfallEl.textContent = weather.rainfall;
    windEl.textContent = weather.windSpeed;
    skyEl.textContent = weather.sky;
    ptyEl.textContent = weather.pty;

    weatherCard.classList.remove("hidden");
}

// 에러 처리
function handleError(err) {
    console.error(err);
    if (err.code) { // GPS 관련 오류
        switch (err.code) {
            case 1: alert("GPS 권한이 거부되었습니다."); break;
            case 2: alert("위치 정보를 찾을 수 없습니다."); break;
            case 3: alert("GPS 요청 시간이 초과되었습니다."); break;
            default: alert("알 수 없는 GPS 오류가 발생했습니다."); break;
        }
    } else {
        alert(err.message || "알 수 없는 오류가 발생했습니다.");
    }
}

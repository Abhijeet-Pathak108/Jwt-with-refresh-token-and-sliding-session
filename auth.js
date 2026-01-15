const API_BASE = "http://localhost:8090";

function saveAccessToken(token) {
    localStorage.setItem("accessToken", token);
}

function getAccessToken() {
    return localStorage.getItem("accessToken");
}

function removeAccessToken() {
    localStorage.removeItem("accessToken");
}

function logout() {
    fetch(`${API_BASE}/auth/logout`, {
        method: "POST",
        credentials: "include"
    }).finally(() => {
        removeAccessToken();
        window.location.href = "login.html";
    });
}

// ---- Refresh Logic ----
function refreshAccessToken() {
    return fetch(`${API_BASE}/auth/refresh`, {
        method: "POST",
        credentials: "include"
    })
    .then(res => {
        if (!res.ok) throw new Error("refresh-failed");
        return res.json();
    })
    .then(data => {
        saveAccessToken(data.accessToken);
        return data.accessToken;
    });
}

// ---- Protected Fetch Wrapper ----
function fetchWithAuth(url, options = {}) {
    let token = getAccessToken();

    return fetch(API_BASE + url, {
        ...options,
         credentials: "include", 
        headers: {
            ...(options.headers || {}),
            "Authorization": "Bearer " + token
        }
    }).then(res => {
        if (res.status !== 401) return res;

        // → token expired → refresh
        return refreshAccessToken().then(newToken => {
            return fetch(API_BASE + url, {
                ...options,
                 credentials: "include", 
                headers: {
                    ...(options.headers || {}),
                    "Authorization": "Bearer " + newToken
                }
            });
        }).catch(() => {
            logout();
        });
    });
}

// function fetchWithAuth(url, options = {}) {
//     let token = getAccessToken();

//     return fetch(`${API_BASE}${url}`, {
//         ...options,
//         headers: {
//             ...(options.headers || {}),
//             Authorization: "Bearer " + token
//         }
//     })
//     .then(res => {
//         if (res.status !== 401) return res;

//         // Try refresh
//         return refreshAccessToken()
//             .then(newToken => {
//                 return fetch(`${API_BASE}${url}`, {
//                     ...options,
//                     headers: {
//                         ...(options.headers || {}),
//                         Authorization: "Bearer " + newToken
//                     }
//                 });
//             })
//             .catch(() => {
//                 logout();
//             });
//     });
// }

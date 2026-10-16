// Base URL: set VITE_API_URL in frontend/.env to change it.
// Local default is your backend on port 8081.
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8081";

async function request(path, options = {}) {
  const res = await fetch(BASE_URL + path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  let data = null;
  try {
    data = await res.json();
  } catch {
    // empty or non-JSON body
  }
  if (!res.ok) {
    throw new Error((data && data.message) || "Request failed (" + res.status + ")");
  }
  return data;
}

// A simple anonymous user id, kept in this browser (there is no login yet).
export function getUserId() {
  let id = localStorage.getItem("navigatorUserId");
  if (!id) {
    id = "user-" + Math.random().toString(36).slice(2, 10);
    localStorage.setItem("navigatorUserId", id);
  }
  return id;
}

// 1. Save a need assessment -> returns the saved assessment (with assessmentId)
// data: { needCategory, location, age, isStudent, incomeLevel, preference }
export function createAssessment(data) {
  return request("/api/assessments", {
    method: "POST",
    body: JSON.stringify({ userId: getUserId(), ...data }),
  });
}

// 2. Ranked results -> [{ matchScore, resource: {...} }]
export function getRecommendations(assessmentId) {
  return request("/api/recommendations?assessmentId=" + encodeURIComponent(assessmentId));
}

// 3. One resource's details
export function getResource(resourceId) {
  return request("/api/resources/" + encodeURIComponent(resourceId));
}

// 4. Request assistance -> returns the referral (with referralId)
export function createReferral(resourceId, assessmentId) {
  return request("/api/referrals", {
    method: "POST",
    body: JSON.stringify({ userId: getUserId(), resourceId, assessmentId }),
  });
}

// 5. My requests
export function getMyReferrals() {
  return request("/api/referrals?userId=" + encodeURIComponent(getUserId()));
}

// 6. Update status: REQUESTED | CONTACTED | RECEIVED | NOT_RECEIVED
// failureReason is required for NOT_RECEIVED:
// NO_RESPONSE | NOT_ELIGIBLE | TOO_FAR | UNAVAILABLE | INCORRECT_INFO
export function updateReferralStatus(referralId, status, failureReason) {
  return request("/api/referrals/" + encodeURIComponent(referralId) + "/status", {
    method: "PUT",
    body: JSON.stringify({ status, failureReason }),
  });
}

// 7. Outcome feedback (rating 1 to 5)
export function submitFeedback(referralId, rating, comment) {
  return request("/api/feedback", {
    method: "POST",
    body: JSON.stringify({ referralId, rating, comment }),
  });
}

// Admin dashboard (Member 5)
export function getAnalytics() {
  return request("/api/admin/analytics");
}
export async function registerUser(name, email, password) {
  return request("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({ name, email, password }),
  });
}

export async function loginUser(email, password) {
  return request("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

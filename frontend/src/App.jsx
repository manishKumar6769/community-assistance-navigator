import AdminAnalytics from "./AdminAnalytics";
import { useState, useEffect } from "react";
import "./App.css";
import {
  createAssessment,
  getRecommendations,
  createReferral,
  getMyReferrals,
  updateReferralStatus,
  submitFeedback,
  loginUser,
  registerUser,
} from "./api";
import { getUserId } from "./api";


const categories = ["Education", "Health", "Food", "Legal", "Employment"];

const FAILURE_REASONS = [
  ["NO_RESPONSE", "No response"],
  ["NOT_ELIGIBLE", "Not eligible"],
  ["TOO_FAR", "Too far"],
  ["UNAVAILABLE", "Unavailable"],
  ["INCORRECT_INFO", "Incorrect information"],
];

const STATUS_LABELS = {
  REQUESTED: "Requested",
  CONTACTED: "Contacted",
  RECEIVED: "Assistance Received",
  NOT_RECEIVED: "Unable to Assist",
};

function App() {
const [showAnalytics, setShowAnalytics] = useState(false);
const [showAuth, setShowAuth] = useState(false);
const [authMode, setAuthMode] = useState("login");
const [authLoading, setAuthLoading] = useState(false);
const [authError, setAuthError] = useState("");
const [currentUser, setCurrentUser] = useState(() => {
  try {
    return JSON.parse(localStorage.getItem("navigatorUser") || "null");
  } catch {
    return null;
  }
});

const [authData, setAuthData] = useState({
  name: "",
  email: "",
  password: "",
});
  const [selectedCategory, setSelectedCategory] = useState("");
  const [assessment, setAssessment] = useState({
    location: "",
    age: "",
    isStudent: false,
    incomeLevel: "LOW",
  });
  const [assessmentId, setAssessmentId] = useState(null);
  const [results, setResults] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);

  const [selectedResource, setSelectedResource] = useState(null);
  const [showRequestForm, setShowRequestForm] = useState(false);
  const [requestData, setRequestData] = useState({
    name: "",
    phone: "",
    reason: "",
    contactMethod: "",
  });
  const [requestSubmitted, setRequestSubmitted] = useState(false);

  const [myRequests, setMyRequests] = useState([]);

  const [feedbackReferralId, setFeedbackReferralId] = useState("");
  const [feedbackRating, setFeedbackRating] = useState("5");
  const [feedbackComment, setFeedbackComment] = useState("");
  const [feedbackSubmitted, setFeedbackSubmitted] = useState(false);

  // Load my requests from the backend

  const handleAuthChange = (event) => {
    const { name, value } = event.target;

    setAuthData((previous) => ({
      ...previous,
      [name]: value,
    }));

    setAuthError("");
  };

  const handleAuthSubmit = async (event) => {
    event.preventDefault();
    setAuthError("");

    if (!authData.email.trim() || !authData.password) {
      setAuthError("Please enter your email and password.");
      return;
    }

    if (
      authMode === "register" &&
      (!authData.name.trim() || authData.password.length < 8)
    ) {
      setAuthError(
        "Enter your name and use a password with at least 8 characters."
      );
      return;
    }

    setAuthLoading(true);

    try {
      const user =
        authMode === "register"
          ? await registerUser(
              authData.name.trim(),
              authData.email.trim(),
              authData.password
            )
          : await loginUser(
              authData.email.trim(),
              authData.password
            );

      setCurrentUser(user);
      localStorage.setItem("navigatorUser", JSON.stringify(user));

      if (user.userId) {
        localStorage.setItem("navigatorUserId", user.userId);
      }

      setShowAuth(false);
      setAuthData({ name: "", email: "", password: "" });
      await loadMyRequests();
    } catch (error) {
      setAuthError(error.message || "Login or registration failed.");
    } finally {
      setAuthLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("navigatorUser");
    localStorage.removeItem("navigatorUserId");
    setCurrentUser(null);
    setMyRequests([]);
    setShowAuth(false);
  };

  const loadMyRequests = async () => {
    try {
      const list = await getMyReferrals();
      const names = JSON.parse(localStorage.getItem("referralNames") || "{}");
      const mapped = list
        .map((r) => ({
          id: r.referralId,
          resourceName: names[r.referralId] || "Resource " + r.resourceId.slice(0, 8),
          status: r.status,
          failureReason: r.failureReason,
          createdAt: r.createdAt,
          date: new Date(r.createdAt).toLocaleDateString(),
        }))
        .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));
      setMyRequests(mapped);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    loadMyRequests();
  }, []);

  const handleFindHelp = async () => {
    if (!selectedCategory) {
      alert("Please select a category first.");
      return;
    }
    if (!assessment.location.trim() || !assessment.age) {
      alert("Please enter your location and age.");
      return;
    }
    if (Number(assessment.age) <= 0) {
      alert("Please enter a valid age.");
      return;
    }

    setLoading(true);
    try {
      const saved = await createAssessment({
        needCategory: selectedCategory,
        location: assessment.location.trim(),
        age: Number(assessment.age),
        isStudent: assessment.isStudent,
        incomeLevel: assessment.incomeLevel,
        preference: "nearest",
      });
      setAssessmentId(saved.assessmentId);
      const ranked = await getRecommendations(saved.assessmentId);
      setResults(ranked);
      setSearched(true);
      setSelectedResource(null);
      setShowRequestForm(false);
      setRequestSubmitted(false);
      setTimeout(() => {
        document.getElementById("results")?.scrollIntoView({ behavior: "smooth" });
      }, 100);
    } catch (e) {
      alert(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitRequest = async (event) => {
    event.preventDefault();

    if (
      !requestData.name.trim() ||
      !requestData.phone.trim() ||
      !requestData.reason.trim() ||
      !requestData.contactMethod
    ) {
      alert("Please fill in all fields.");
      return;
    }

    try {
      const referral = await createReferral(
        selectedResource.resourceId,
        assessmentId
      );
      // Remember the resource name in this browser for display
      const names = JSON.parse(localStorage.getItem("referralNames") || "{}");
      names[referral.referralId] = selectedResource.name;
      localStorage.setItem("referralNames", JSON.stringify(names));

      await loadMyRequests();
      setRequestSubmitted(true);
    } catch (e) {
      alert(e.message);
    }
  };

  const handleStatusChange = async (request, status) => {
    try {
      const reason = status === "NOT_RECEIVED" ? "NO_RESPONSE" : undefined;
      await updateReferralStatus(request.id, status, reason);
      await loadMyRequests();
    } catch (e) {
      alert(e.message);
    }
  };

  const handleReasonChange = async (request, reason) => {
    try {
      await updateReferralStatus(request.id, "NOT_RECEIVED", reason);
      await loadMyRequests();
    } catch (e) {
      alert(e.message);
    }
  };

  const handleSubmitFeedback = async (event) => {
    event.preventDefault();
    if (!feedbackReferralId) {
      alert("Please choose which request your feedback is about.");
      return;
    }
    try {
      await submitFeedback(
        feedbackReferralId,
        Number(feedbackRating),
        feedbackComment
      );
      setFeedbackSubmitted(true);
    } catch (e) {
      alert(e.message);
    }
  };

  const formStyle = {
    display: "flex",
    flexWrap: "wrap",
    gap: "12px",
    justifyContent: "center",
    margin: "20px 0",
  };

  return (
    <div className="app">
      {/* Navbar */}
      <header className="navbar">
        <div className="logo">Community Assistance Navigator</div>

        <nav>
          <a href="#">Home</a>
          <a href="#results">Find Help</a>
          <a href="#my-requests">My Requests</a>
          <button
  className="login-btn"
  onClick={() => {
    setShowAnalytics((previous) => !previous);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }}
>
  {showAnalytics ? "Back to Home" : "Admin Analytics"}
</button>



{currentUser ? (
  <button
    className="login-btn"
    onClick={handleLogout}
  >
    Logout
  </button>
) : (
  <button
    className="login-btn"
    onClick={() => {
      setAuthMode("login");
      setAuthError("");
      setShowAuth(true);
    }}
  >
    Login
  </button>
)}


        </nav>
      </header>
      {showAnalytics && <AdminAnalytics />}

      {/* Hero */}
      <main className="hero">
        <p className="tagline">Find the right help. Follow it through.</p>

        <h1>What do you need help with?</h1>

        <p className="subtitle">
          Tell us what you need and we'll help you find suitable community
          resources.
        </p>

        {/* Categories */}
        <div className="categories">
          {categories.map((category) => (
            <button
              key={category}
              className={
                selectedCategory === category ? "category selected" : "category"
              }
              onClick={() => setSelectedCategory(category)}
            >
              {category}
            </button>
          ))}
        </div>

        {selectedCategory && (
          <div className="selected-message">
            You selected: <strong>{selectedCategory}</strong>
          </div>
        )}

        {/* Need assessment */}
        <div style={formStyle}>
          <input
            type="text"
            placeholder="Your location (e.g. Jamshedpur)"
            value={assessment.location}
            onChange={(e) =>
              setAssessment({ ...assessment, location: e.target.value })
            }
          />
          <input
            type="number"
            min="1"
            placeholder="Your age"
            value={assessment.age}
            onChange={(e) =>
              setAssessment({ ...assessment, age: e.target.value })
            }
          />
          <label>
            <input
              type="checkbox"
              checked={assessment.isStudent}
              onChange={(e) =>
                setAssessment({ ...assessment, isStudent: e.target.checked })
              }
            />{" "}
            I am a student
          </label>
          <select
            value={assessment.incomeLevel}
            onChange={(e) =>
              setAssessment({ ...assessment, incomeLevel: e.target.value })
            }
          >
            <option value="LOW">Low income</option>
            <option value="MEDIUM">Medium income</option>
            <option value="HIGH">High income</option>
          </select>
        </div>

        <button
          className="find-help-btn"
          onClick={handleFindHelp}
          disabled={loading}
        >
          {loading ? "Searching..." : "Find Help →"}
        </button>
      </main>

      {/* Results */}
      {searched && (
        <section className="results-section" id="results">
          <p className="results-label">AVAILABLE RESOURCES</p>

          <h2>Help available for {selectedCategory}</h2>

          <p className="results-subtitle">
            Ranked by how well they match your situation.
          </p>

          {results.length === 0 ? (
            <p>No resources found for this category yet.</p>
          ) : (
            <div className="resource-grid">
              {results.map((item) => (
                <div className="resource-card" key={item.resource.resourceId}>
                  <span className="resource-type">
                    {item.resource.category} · Match {item.matchScore}%
                  </span>

                  <h3>{item.resource.name}</h3>

                  <p>{item.resource.services}</p>
                  <p>
                    <strong>Location:</strong> {item.resource.location}
                  </p>

                  <button
                    className="details-btn"
                    onClick={() => {
                      setSelectedResource(item.resource);
                      setShowRequestForm(false);
                      setRequestSubmitted(false);
                      setTimeout(() => {
                        document
                          .querySelector(".resource-details")
                          ?.scrollIntoView({ behavior: "smooth", block: "start" });
                      }, 100);
                    }}
                  >
                    View Details →
                  </button>
                </div>
              ))}
            </div>
          )}
        </section>
      )}

      {/* Resource Details */}
      {selectedResource && (
        <section className="resource-details">
          <button
            className="back-btn"
            onClick={() => setSelectedResource(null)}
          >
            ← Back to Results
          </button>

          <div className="details-container">
            <span className="resource-type">{selectedResource.category}</span>

            <h2>{selectedResource.name}</h2>

            <p className="details-description">{selectedResource.services}</p>

            <div className="details-grid">
              <div className="detail-box">
                <h4>Services</h4>
                <p>{selectedResource.services}</p>
              </div>

              <div className="detail-box">
                <h4>Eligibility</h4>
                <p>
                  Age {selectedResource.minAge} to {selectedResource.maxAge}
                  {selectedResource.studentOnly ? ", students only" : ""}
                  {selectedResource.maxIncomeLevel
                    ? ", income up to " + selectedResource.maxIncomeLevel
                    : ""}
                </p>
              </div>

              <div className="detail-box">
                <h4>Availability</h4>
                <p>{selectedResource.availability}</p>
              </div>

              <div className="detail-box">
                <h4>Reliability</h4>
                <p>
                  Score: {selectedResource.reliabilityScore}/100
                  <br />
                  Status: {selectedResource.verificationStatus}
                  <br />
                  Last verified: {selectedResource.lastVerifiedDate}
                </p>
              </div>

              <div className="detail-box">
                <h4>Contact</h4>
                <p>{selectedResource.contact}</p>
              </div>
            </div>

            <button
              className="request-btn"
              onClick={() => setShowRequestForm(true)}
            >
              Request Assistance →
            </button>
          </div>
        </section>
      )}

      {/* Request Assistance Form */}
      {showRequestForm && !requestSubmitted && (
        <section className="request-section">
          <button
            className="back-btn"
            onClick={() => setShowRequestForm(false)}
          >
            ← Back to Resource
          </button>

          <div className="request-container">
            <p className="results-label">REQUEST ASSISTANCE</p>

            <h2>Request Help</h2>

            <p className="request-subtitle">
              Submit a request for <strong>{selectedResource?.name}</strong>.
            </p>

            <form className="request-form" onSubmit={handleSubmitRequest}>
              <label>Full Name</label>
              <input
                type="text"
                placeholder="Enter your full name"
                value={requestData.name}
                onChange={(e) =>
                  setRequestData({ ...requestData, name: e.target.value })
                }
                required
              />

              <label>Phone Number</label>
              <input
                type="tel"
                pattern="[6-9][0-9]{9}"
                maxLength={10}
                placeholder="Enter your phone number"
                value={requestData.phone}
                onChange={(e) =>
                  setRequestData({ ...requestData, phone: e.target.value })
                }
                required
              />

              <label>Reason for Assistance</label>
              <textarea
                placeholder="Briefly explain what help you need..."
                rows="4"
                value={requestData.reason}
                onChange={(e) =>
                  setRequestData({ ...requestData, reason: e.target.value })
                }
                required
              ></textarea>

              <label>Preferred Contact Method</label>
              <select
                value={requestData.contactMethod}
                onChange={(e) =>
                  setRequestData({
                    ...requestData,
                    contactMethod: e.target.value,
                  })
                }
                required
              >
                <option value="">Select an option</option>
                <option value="phone">Phone</option>
                <option value="email">Email</option>
              </select>

              <button type="submit" className="submit-request-btn">
                Submit Request →
              </button>
            </form>
          </div>
        </section>
      )}

      {requestSubmitted && (
        <section className="request-section">
          <div className="request-container request-success">
            <h2>Request submitted successfully!</h2>

            <p>
              Your request for <strong>{selectedResource?.name}</strong> has
              been recorded.
            </p>

            <p>
              Current status: <strong>Requested</strong>
            </p>

            <button
              className="submit-request-btn"
              onClick={() => {
                setRequestSubmitted(false);
                setShowRequestForm(false);
                setRequestData({
                  name: "",
                  phone: "",
                  reason: "",
                  contactMethod: "",
                });
                document
                  .getElementById("my-requests")
                  ?.scrollIntoView({ behavior: "smooth" });
              }}
            >
              Continue →
            </button>
          </div>
        </section>
      )}

      {/* My Requests Section */}
      <section id="my-requests" className="request-section">
        <div className="request-container">
          <h2>My Requests</h2>

          {myRequests.length === 0 ? (
            <p>You haven't submitted any requests yet.</p>
          ) : (
            myRequests.map((request) => (
              <div className="resource-card" key={request.id}>
                <h3>{request.resourceName}</h3>
                <p>Request ID: {request.id}</p>
                <label>
                  Update status:{" "}
                  <select
                    value={request.status}
                    onChange={(event) =>
                      handleStatusChange(request, event.target.value)
                    }
                  >
                    {Object.entries(STATUS_LABELS).map(([value, label]) => (
                      <option key={value} value={value}>
                        {label}
                      </option>
                    ))}
                  </select>
                </label>

                {request.status === "NOT_RECEIVED" && (
                  <label style={{ display: "block", marginTop: "8px" }}>
                    Reason:{" "}
                    <select
                      value={request.failureReason || "NO_RESPONSE"}
                      onChange={(event) =>
                        handleReasonChange(request, event.target.value)
                      }
                    >
                      {FAILURE_REASONS.map(([value, label]) => (
                        <option key={value} value={value}>
                          {label}
                        </option>
                      ))}
                    </select>
                  </label>
                )}

                <p>Date: {request.date}</p>
              </div>
            ))
          )}
        </div>
      </section>

      {/* Feedback Section */}
      <section id="feedback" className="request-section">
        <div className="request-container feedback-container">
          <h2>Share Your Feedback</h2>
          {feedbackSubmitted ? (
            <>
              <p>Thank you for your feedback!</p>
              <button
                className="submit-request-btn"
                onClick={() => {
                  setFeedbackSubmitted(false);
                  setFeedbackComment("");
                  setFeedbackRating("5");
                  setFeedbackReferralId("");
                }}
              >
                Give more feedback
              </button>
            </>
          ) : myRequests.length === 0 ? (
            <p>Submit a request first, then you can share feedback about it.</p>
          ) : (
            <form onSubmit={handleSubmitFeedback}>
              <label>
                About which request:
                <select
                  value={feedbackReferralId}
                  onChange={(event) => setFeedbackReferralId(event.target.value)}
                >
                  <option value="">Select a request</option>
                  {myRequests.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.resourceName} ({r.date})
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Rating:
                <select
                  value={feedbackRating}
                  onChange={(event) => setFeedbackRating(event.target.value)}
                >
                  <option value="5">5 - Excellent</option>
                  <option value="4">4 - Good</option>
                  <option value="3">3 - Average</option>
                  <option value="2">2 - Poor</option>
                  <option value="1">1 - Very Poor</option>
                </select>
              </label>

              <label>
                Comments:
                <textarea
                  value={feedbackComment}
                  onChange={(event) => setFeedbackComment(event.target.value)}
                  placeholder="Tell us about your experience"
                  rows={4}
                />
              </label>

              <button type="submit" className="submit-request-btn">
                Submit Feedback
              </button>
            </form>
          )}
        </div>
      </section>

      <footer>
        <p>Community Assistance Navigator</p>

        <p>Find the right help. Follow it through. Learn what works.</p>
      </footer>

{showAuth && (
  <div className="auth-overlay">
    <div className="auth-modal">
      <button
        type="button"
        className="auth-close"
        onClick={() => {
          setShowAuth(false);
          setAuthError("");
        }}
      >
        ✕
      </button>

      <h2>
        {authMode === "login" ? "Welcome Back" : "Create Your Account"}
      </h2>

      <p>
        {authMode === "login"
          ? "Log in to continue finding community support."
          : "Register to get started."}
      </p>

      <form onSubmit={handleAuthSubmit}>
        {authMode === "register" && (
          <>
            <label htmlFor="auth-name">Full Name</label>
            <input
              id="auth-name"
              name="name"
              type="text"
              placeholder="Enter your name"
              value={authData.name}
              onChange={handleAuthChange}
              required
            />
          </>
        )}

        <label htmlFor="auth-email">Email Address</label>
        <input
          id="auth-email"
          name="email"
          type="email"
          placeholder="Enter your email"
          value={authData.email}
          onChange={handleAuthChange}
          required
        />

        <label htmlFor="auth-password">Password</label>
        <input
          id="auth-password"
          name="password"
          type="password"
          placeholder="Enter your password"
          value={authData.password}
          onChange={handleAuthChange}
          minLength={authMode === "register" ? 8 : undefined}
          required
        />

        {authError && (
          <p className="auth-error" role="alert">{authError}</p>
        )}

        <button
          type="submit"
          className="submit-request-btn"
          disabled={authLoading}
        >
          {authLoading
            ? "Please wait..."
            : authMode === "login"
              ? "Login"
              : "Create Account"}
        </button>
      </form>

      <p className="auth-switch">
        {authMode === "login"
          ? "Don't have an account?"
          : "Already have an account?"}{" "}
        <button
          type="button"
          onClick={() => {
            setAuthMode(authMode === "login" ? "register" : "login");
            setAuthError("");
          }}
        >
          {authMode === "login" ? "Register" : "Login"}
        </button>
      </p>
    </div>
  </div>
)}

    </div>
  );
}

export default App;

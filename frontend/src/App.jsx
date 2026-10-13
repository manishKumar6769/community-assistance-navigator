
import { useEffect, useMemo, useState } from "react";
import "./App.css";

const API_BASE_URL = "http://localhost:8081";
const CATEGORIES = [
  "Food Assistance",
  "Financial Assistance",
  "Education",
  "Healthcare",
  "Employment",
];

const CATEGORY_MAPPING = {
  "Food Assistance": "Food",
  Education: "Education",
  Healthcare: "Health",
  Employment: "Employment",
  "Financial Assistance": "Financial Assistance",
};

const EMPTY_REQUEST = {
  name: "",
  phone: "",
  reason: "",
  contactMethod: "",
};

const STATUS_LABELS = {
  REQUESTED: "Requested",
  CONTACTED: "Contacted",
  RECEIVED: "Assistance Received",
  NOT_RECEIVED: "Unable to Assist",
};

const FAILURE_REASONS = [
  "NO_RESPONSE",
  "NOT_ELIGIBLE",
  "TOO_FAR",
  "UNAVAILABLE",
  "INCORRECT_INFO",
];

function getUserId() {
  let id = localStorage.getItem("navigatorUserId");

  if (!id) {
    id = `user-${crypto.randomUUID()}`;
    localStorage.setItem("navigatorUserId", id);
  }

  return id;
}

async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    throw new Error(
      data?.message || data?.error || `Request failed (${response.status})`
    );
  }

  return data;
}

function App() {
  const [userId] = useState(getUserId);

  const [selectedCategory, setSelectedCategory] = useState("");
  const [selectedResource, setSelectedResource] = useState(null);
  const [showRequestForm, setShowRequestForm] = useState(false);
  const [requestData, setRequestData] = useState(EMPTY_REQUEST);
  const [requestSubmitted, setRequestSubmitted] = useState(false);

  const [feedbackRating, setFeedbackRating] = useState("5");
  const [feedbackComment, setFeedbackComment] = useState("");
  const [feedbackReferralId, setFeedbackReferralId] = useState("");
  const [feedbackSubmitted, setFeedbackSubmitted] = useState(false);
  const [feedbackLoading, setFeedbackLoading] = useState(false);
  const [feedbackError, setFeedbackError] = useState("");

  const [myRequests, setMyRequests] = useState([]);
  const [loadingRequests, setLoadingRequests] = useState(false);
  const [requestError, setRequestError] = useState("");
  const [submittingRequest, setSubmittingRequest] = useState(false);
  const [statusUpdatingId, setStatusUpdatingId] = useState("");

  const [backendResources, setBackendResources] = useState([]);
  const [loadingResources, setLoadingResources] = useState(true);
  const [resourceError, setResourceError] = useState("");

  const [notice, setNotice] = useState("");

  const resourceById = useMemo(
    () =>
      Object.fromEntries(
        backendResources.map((resource) => [
          resource.resourceId,
          resource,
        ])
      ),
    [backendResources]
  );

  const filteredResources = useMemo(() => {
    const category = CATEGORY_MAPPING[selectedCategory];

    if (!category) return [];

    return backendResources.filter(
      (resource) => resource.category === category
    );
  }, [backendResources, selectedCategory]);

  async function loadResources() {
    setLoadingResources(true);
    setResourceError("");

    try {
      const data = await apiRequest("/api/resources");

      if (!Array.isArray(data)) {
        throw new Error("Resources API did not return a list.");
      }

      setBackendResources(data);
    } catch (error) {
      setResourceError(error.message);
    } finally {
      setLoadingResources(false);
    }
  }

  async function loadRequests() {
    setLoadingRequests(true);
    setRequestError("");

    try {
      const data = await apiRequest(
        `/api/referrals?userId=${encodeURIComponent(userId)}`
      );

      if (!Array.isArray(data)) {
        throw new Error("Referrals API did not return a list.");
      }

      setMyRequests(data);
    } catch (error) {
      setRequestError(error.message);
    } finally {
      setLoadingRequests(false);
    }
  }

  
useEffect(() => {
  const controller = new AbortController();

  async function fetchInitialData() {
    try {
      const [resourcesResponse, requestsResponse] = await Promise.all([
        fetch(`${API_BASE_URL}/api/resources`, {
          signal: controller.signal,
        }),
        fetch(
          `${API_BASE_URL}/api/referrals?userId=${encodeURIComponent(userId)}`,
          { signal: controller.signal }
        ),
      ]);

      if (!resourcesResponse.ok) {
        throw new Error(`Resources request failed (${resourcesResponse.status})`);
      }

      if (!requestsResponse.ok) {
        throw new Error(`Requests request failed (${requestsResponse.status})`);
      }

      const [resources, requests] = await Promise.all([
        resourcesResponse.json(),
        requestsResponse.json(),
      ]);

      if (!Array.isArray(resources) || !Array.isArray(requests)) {
        throw new Error("Backend returned an invalid response.");
      }

      if (!controller.signal.aborted) {
        setBackendResources(resources);
        setMyRequests(requests);
        setResourceError("");
        setRequestError("");
      }
    } catch (error) {
      if (error.name !== "AbortError" && !controller.signal.aborted) {
        setResourceError(error.message);
        setRequestError(error.message);
      }
    } finally {
      if (!controller.signal.aborted) {
        setLoadingResources(false);
        setLoadingRequests(false);
      }
    }
  }

  fetchInitialData();

  return () => controller.abort();
}, [userId]);


  async function handleSubmitRequest(event) {
    event.preventDefault();

    if (!selectedResource || submittingRequest) return;

    if (
      !requestData.name.trim() ||
      !requestData.phone.trim() ||
      !requestData.reason.trim() ||
      !requestData.contactMethod
    ) {
      setNotice("Please fill in all fields.");
      return;
    }

    setSubmittingRequest(true);
    setNotice("");

    try {
      const referral = await apiRequest("/api/referrals", {
        method: "POST",
        body: JSON.stringify({
          userId,
          resourceId: selectedResource.resourceId,
        }),
      });

      setMyRequests((previous) => [referral, ...previous]);
      setFeedbackReferralId(referral.referralId || "");
      setRequestSubmitted(true);
      setNotice("");

      // The current backend Referral model does not store
      // name, phone, reason or contact preference.
    } catch (error) {
      setNotice(`Could not submit request: ${error.message}`);
    } finally {
      setSubmittingRequest(false);
    }
  }

  async function updateRequestStatus(request, status) {
    if (statusUpdatingId) return;

    let failureReason;

    if (status === "NOT_RECEIVED") {
      failureReason = window.prompt(
        `Choose a failure reason:\n${FAILURE_REASONS.join("\n")}`,
        "NO_RESPONSE"
      );

      if (failureReason === null) return;

      failureReason = failureReason.trim().toUpperCase();

      if (!FAILURE_REASONS.includes(failureReason)) {
        setRequestError("Invalid failure reason. Please try again.");
        return;
      }
    }

    setStatusUpdatingId(request.referralId);
    setRequestError("");

    try {
      const updated = await apiRequest(
        `/api/referrals/${encodeURIComponent(request.referralId)}/status`,
        {
          method: "PUT",
          body: JSON.stringify({
            status,
            ...(failureReason ? { failureReason } : {}),
          }),
        }
      );

      setMyRequests((previous) =>
        previous.map((item) =>
          item.referralId === updated.referralId ? updated : item
        )
      );

      setNotice("Request status updated successfully.");
    } catch (error) {
      setRequestError(`Could not update status: ${error.message}`);
    } finally {
      setStatusUpdatingId("");
    }
  }

  async function handleSubmitFeedback(event) {
    event.preventDefault();

    if (!feedbackReferralId) {
      setFeedbackError(
        "Submit a help request first, then select its referral ID here."
      );
      return;
    }

    setFeedbackLoading(true);
    setFeedbackError("");

    try {
      await apiRequest("/api/feedback", {
        method: "POST",
        body: JSON.stringify({
          referralId: feedbackReferralId,
          resourceId:
            myRequests.find(
              (item) => item.referralId === feedbackReferralId
            )?.resourceId || "",
          rating: Number(feedbackRating),
          comment: feedbackComment.trim(),
        }),
      });

      setFeedbackSubmitted(true);
    } catch (error) {
      setFeedbackError(`Feedback submission failed: ${error.message}`);
    } finally {
      setFeedbackLoading(false);
    }
  }

  function resetRequestForm() {
    setRequestSubmitted(false);
    setShowRequestForm(false);
    setRequestData(EMPTY_REQUEST);
    setNotice("");
  }

  function openResourceDetails(resource) {
    setSelectedResource(resource);
    setShowRequestForm(false);
    setRequestSubmitted(false);

    window.setTimeout(() => {
      document
        .querySelector(".resource-details")
        ?.scrollIntoView({ behavior: "smooth", block: "start" });
    }, 100);
  }

  return (
    <div className="app">
      <header className="navbar">
        <div className="logo">Community Assistance Navigator</div>
        <nav>
          <a href="#">Home</a>
          <a href="#results">Find Help</a>
          <a href="#my-requests">My Requests</a>
          <button
            className="login-btn"
            type="button"
            onClick={() => alert("Login is not implemented yet.")}
          >
            Login
          </button>
        </nav>
      </header>

      <main className="hero">
        <p className="tagline">Find the right help. Follow it through.</p>
        <h1>What do you need help with?</h1>
        <p className="subtitle">
          Tell us what you need and we&apos;ll help you find suitable community
          resources.
        </p>

        <div className="categories">
          {CATEGORIES.map((category) => (
            <button
              key={category}
              type="button"
              className={
                selectedCategory === category
                  ? "category selected"
                  : "category"
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

        <button
          className="find-help-btn"
          type="button"
          onClick={() => {
            if (!selectedCategory) {
              alert("Please select a category first.");
              return;
            }

            document
              .getElementById("results")
              ?.scrollIntoView({ behavior: "smooth" });
          }}
        >
          Find Help →
        </button>
      </main>

      {selectedCategory && (
        <section className="results-section" id="results">
          <p className="results-label">AVAILABLE RESOURCES</p>
          <h2>Help available for {selectedCategory}</h2>
          <p className="results-subtitle">
            Explore resources that may help with your needs.
          </p>

          <div className="resource-grid">
            {loadingResources ? (
              <p>Loading resources...</p>
            ) : resourceError ? (
              <div role="alert">
                <p>{resourceError}</p>
                <button
                  className="details-btn"
                  type="button"
                  onClick={loadResources}
                >
                  Retry
                </button>
              </div>
            ) : !CATEGORY_MAPPING[selectedCategory] ? (
              <p>
                Resources for {selectedCategory} are not available in the
                backend yet.
              </p>
            ) : filteredResources.length === 0 ? (
              <p>No resources found for this category.</p>
            ) : (
              filteredResources.map((resource) => (
                <article
                  className="resource-card"
                  key={resource.resourceId}
                >
                  <span className="resource-type">{resource.category}</span>
                  <h3>{resource.name}</h3>
                  <p>{resource.services}</p>
                  <p>Location: {resource.location}</p>
                  <p>Availability: {resource.availability}</p>
                  <p>Reliability: {resource.reliabilityScore}/100</p>
                  <button
                    className="details-btn"
                    type="button"
                    onClick={() => openResourceDetails(resource)}
                  >
                    View Details →
                  </button>
                </article>
              ))
            )}
          </div>
        </section>
      )}

      {selectedResource && (
        <section className="resource-details">
          <button
            className="back-btn"
            type="button"
            onClick={() => {
              setSelectedResource(null);
              setShowRequestForm(false);
              setRequestSubmitted(false);
            }}
          >
            ← Back to Results
          </button>

          <div className="details-container">
            <span className="resource-type">
              {selectedResource.category}
            </span>
            <h2>{selectedResource.name}</h2>
            <p className="details-description">
              {selectedResource.services}
            </p>

            <div className="details-grid">
              <div className="detail-box">
                <h4>Services</h4>
                <p>{selectedResource.services || "Not provided"}</p>
              </div>
              <div className="detail-box">
                <h4>Location</h4>
                <p>{selectedResource.location || "Not provided"}</p>
              </div>
              <div className="detail-box">
                <h4>Eligibility</h4>
                <p>
                  Age: {selectedResource.minAge}–{selectedResource.maxAge}
                  <br />
                  Student only:{" "}
                  {selectedResource.studentOnly ? "Yes" : "No"}
                  <br />
                  Maximum income:{" "}
                  {selectedResource.maxIncomeLevel || "Not specified"}
                </p>
              </div>
              <div className="detail-box">
                <h4>Availability</h4>
                <p>{selectedResource.availability || "Not provided"}</p>
              </div>
              <div className="detail-box">
                <h4>Reliability</h4>
                <p>{selectedResource.reliabilityScore}/100</p>
                <p>
                  Verification: {selectedResource.verificationStatus}
                </p>
                <p>
                  Last verified: {selectedResource.lastVerifiedDate}
                </p>
              </div>
              <div className="detail-box">
                <h4>Contact</h4>
                <p>{selectedResource.contact || "Not provided"}</p>
              </div>
            </div>

            <button
              className="request-btn"
              type="button"
              onClick={() => {
                setShowRequestForm(true);
                setRequestSubmitted(false);
                setNotice("");
              }}
            >
              Request Assistance →
            </button>
          </div>
        </section>
      )}

      {showRequestForm && selectedResource && !requestSubmitted && (
        <section className="request-section">
          <button
            className="back-btn"
            type="button"
            onClick={() => setShowRequestForm(false)}
          >
            ← Back to Resource
          </button>

          <div className="request-container">
            <p className="results-label">REQUEST ASSISTANCE</p>
            <h2>Request Help</h2>
            <p className="request-subtitle">
              Submit a request for <strong>{selectedResource.name}</strong>.
            </p>

            <form className="request-form" onSubmit={handleSubmitRequest}>
              <label htmlFor="request-name">Full Name</label>
              <input
                id="request-name"
                type="text"
                placeholder="Enter your full name"
                value={requestData.name}
                onChange={(event) =>
                  setRequestData({
                    ...requestData,
                    name: event.target.value,
                  })
                }
                required
              />

              <label htmlFor="request-phone">Phone Number</label>
              <input
                id="request-phone"
                type="tel"
                pattern="[6-9][0-9]{9}"
                maxLength={10}
                placeholder="Enter your phone number"
                value={requestData.phone}
                onChange={(event) =>
                  setRequestData({
                    ...requestData,
                    phone: event.target.value,
                  })
                }
                required
              />

              <label htmlFor="request-reason">Reason for Assistance</label>
              <textarea
                id="request-reason"
                placeholder="Briefly explain what help you need..."
                rows={4}
                value={requestData.reason}
                onChange={(event) =>
                  setRequestData({
                    ...requestData,
                    reason: event.target.value,
                  })
                }
                required
              />

              <label htmlFor="contact-method">
                Preferred Contact Method
              </label>
              <select
                id="contact-method"
                value={requestData.contactMethod}
                onChange={(event) =>
                  setRequestData({
                    ...requestData,
                    contactMethod: event.target.value,
                  })
                }
                required
              >
                <option value="">Select an option</option>
                <option value="phone">Phone</option>
                <option value="email">Email</option>
              </select>

              <button
                type="submit"
                disabled={submittingRequest}
                className="submit-request-btn"
              >
                {submittingRequest ? "Submitting..." : "Submit Request →"}
              </button>
            </form>

            <p className="request-subtitle">
              Your referral will be saved by the backend. Note: the current
              backend model does not store the name, phone, reason, or contact
              preference fields from this form.
            </p>
            {notice && <p role="alert">{notice}</p>}
          </div>
        </section>
      )}

      {requestSubmitted && selectedResource && (
        <section className="request-section">
          <div className="request-container request-success">
            <h2>Request submitted successfully!</h2>
            <p>
              Your request for <strong>{selectedResource.name}</strong> has
              been recorded by the backend.
            </p>
            <p>
              Current status: <strong>Requested</strong>
            </p>
            <button
              className="submit-request-btn"
              type="button"
              onClick={resetRequestForm}
            >
              Continue →
            </button>
          </div>
        </section>
      )}

      <section id="my-requests" className="request-section">
        <div className="request-container">
          <h2>My Requests</h2>
          <p className="request-subtitle">
            Your referrals are loaded from the backend.
          </p>

          <button
            className="details-btn"
            type="button"
            onClick={loadRequests}
            disabled={loadingRequests}
          >
            {loadingRequests ? "Refreshing..." : "Refresh Requests"}
          </button>

          {requestError && <p role="alert">{requestError}</p>}

          {loadingRequests ? (
            <p>Loading requests...</p>
          ) : myRequests.length === 0 ? (
            <p>You haven&apos;t submitted any requests yet.</p>
          ) : (
            myRequests.map((request) => {
              const resource = resourceById[request.resourceId];

              return (
                <div
                  className="resource-card"
                  key={request.referralId}
                >
                  <h3>{resource?.name || "Community resource"}</h3>
                  <p>Request ID: {request.referralId}</p>
                  <p>
                    Status:{" "}
                    {STATUS_LABELS[request.status] || request.status}
                  </p>
                  <p>
                    Created:{" "}
                    {request.createdAt
                      ? new Date(request.createdAt).toLocaleString()
                      : "Not available"}
                  </p>

                  <label htmlFor={`status-${request.referralId}`}>
                    Update status:
                  </label>
                  <select
                    id={`status-${request.referralId}`}
                    value={request.status}
                    disabled={statusUpdatingId === request.referralId}
                    onChange={(event) =>
                      updateRequestStatus(request, event.target.value)
                    }
                  >
                    <option value="REQUESTED">Requested</option>
                    <option value="CONTACTED">Contacted</option>
                    <option value="RECEIVED">Assistance Received</option>
                    <option value="NOT_RECEIVED">Unable to Assist</option>
                  </select>

                  <button
                    className="details-btn"
                    type="button"
                    onClick={() => {
                      setFeedbackReferralId(request.referralId);
                      document
                        .getElementById("feedback")
                        ?.scrollIntoView({ behavior: "smooth" });
                    }}
                  >
                    Give Feedback for This Request
                  </button>
                </div>
              );
            })
          )}
        </div>
      </section>

      <section id="feedback" className="request-section">
        <div className="request-container feedback-container">
          <h2>Share Your Feedback</h2>

          {feedbackSubmitted ? (
            <div>
              <p>Thank you! Your feedback was saved by the backend.</p>
              <button
                className="details-btn"
                type="button"
                onClick={() => {
                  setFeedbackSubmitted(false);
                  setFeedbackComment("");
                  setFeedbackRating("5");
                  setFeedbackError("");
                }}
              >
                Submit Another Feedback
              </button>
            </div>
          ) : (
            <form onSubmit={handleSubmitFeedback}>
              <label htmlFor="feedback-referral">Request</label>
              <select
                id="feedback-referral"
                value={feedbackReferralId}
                onChange={(event) =>
                  setFeedbackReferralId(event.target.value)
                }
                required
              >
                <option value="">Select your request</option>
                {myRequests.map((request) => (
                  <option
                    key={request.referralId}
                    value={request.referralId}
                  >
                    {(resourceById[request.resourceId]?.name ||
                      "Community resource") +
                      " — " +
                      request.referralId}
                  </option>
                ))}
              </select>

              <label htmlFor="feedback-rating">Rating:</label>
              <select
                id="feedback-rating"
                value={feedbackRating}
                onChange={(event) =>
                  setFeedbackRating(event.target.value)
                }
              >
                <option value="5">5 - Excellent</option>
                <option value="4">4 - Good</option>
                <option value="3">3 - Average</option>
                <option value="2">2 - Poor</option>
                <option value="1">1 - Very Poor</option>
              </select>

              <label htmlFor="feedback-comment">Comments:</label>
              <textarea
                id="feedback-comment"
                value={feedbackComment}
                onChange={(event) =>
                  setFeedbackComment(event.target.value)
                }
                placeholder="Tell us about your experience"
                rows={4}
              />

              {feedbackError && <p role="alert">{feedbackError}</p>}

              <button
                type="submit"
                className="submit-request-btn"
                disabled={feedbackLoading}
              >
                {feedbackLoading ? "Submitting..." : "Submit Feedback"}
              </button>
            </form>
          )}
        </div>
      </section>

      {notice && !showRequestForm && !requestSubmitted && (
        <p role="status" className="request-subtitle">
          {notice}
        </p>
      )}

      <footer>
        <p>Community Assistance Navigator</p>
        <p>Find the right help. Follow it through. Learn what works.</p>
      </footer>
    </div>
  );
}

export default App;

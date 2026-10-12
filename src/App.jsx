import { useState } from "react";
import "./App.css";

function App() {
  const [selectedCategory, setSelectedCategory] = useState("");
  const [selectedResource, setSelectedResource] = useState(null);
  const [showRequestForm, setShowRequestForm] = useState(false);
  const [requestData, setRequestData] = useState({
  name: "",
  phone: "",
  reason: "",
  contactMethod: "",
});

const [requestSubmitted, setRequestSubmitted] = useState(false);
const [feedbackRating, setFeedbackRating] = useState("5");
const [feedbackComment, setFeedbackComment] = useState("");
const [feedbackSubmitted, setFeedbackSubmitted] = useState(false);
const [myRequests, setMyRequests] = useState(() => {
  const savedRequests = localStorage.getItem("myRequests");
  return savedRequests ? JSON.parse(savedRequests) : [];
});

  const categories = [
    "Food Assistance",
    "Financial Assistance",
    "Education",
    "Healthcare",
    "Employment",
  ];

  const resources = {
    "Food Assistance": [
      {
        name: "Community Food Support",
        description: "Get information about nearby food assistance programs.",
        type: "Food Support",
      },
      {
        name: "Food Distribution Centers",
        description: "Find food distribution and community meal services.",
        type: "Food Service",
      },
      {
        name: "Government Food Schemes",
        description: "Explore available food-related government assistance.",
        type: "Government Support",
      },
    ],

    "Financial Assistance": [
      {
        name: "Financial Aid Programs",
        description: "Explore financial assistance and support programs.",
        type: "Financial Support",
      },
      {
        name: "Government Assistance",
        description: "Find government schemes that may provide financial support.",
        type: "Government Support",
      },
      {
        name: "Emergency Financial Help",
        description: "Information about emergency financial assistance.",
        type: "Emergency Support",
      },
    ],

    Education: [
      {
        name: "Scholarship Programs",
        description: "Find scholarships and educational financial support.",
        type: "Scholarship",
      },
      {
        name: "Education Support",
        description: "Explore programs that help students continue their education.",
        type: "Education",
      },
      {
        name: "Student Resources",
        description: "Find useful resources and support for students.",
        type: "Student Support",
      },
    ],

    Healthcare: [
      {
        name: "Government Health Services",
        description: "Find information about government healthcare services.",
        type: "Healthcare",
      },
      {
        name: "Low-Cost Medical Services",
        description: "Explore affordable healthcare and medical assistance.",
        type: "Medical Support",
      },
      {
        name: "Health Assistance Programs",
        description: "Find community and government health assistance programs.",
        type: "Health Support",
      },
    ],

    Employment: [
      {
        name: "Job Assistance",
        description: "Find employment support and job-related resources.",
        type: "Employment",
      },
      {
        name: "Skill Development Programs",
        description: "Explore training and skill development opportunities.",
        type: "Training",
      },
      {
        name: "Career Support",
        description: "Find resources to improve your employment opportunities.",
        type: "Career Support",
      },
    ],
  };

  const handleFindHelp = () => {
    if (!selectedCategory) {
      alert("Please select a category first.");
      return;
    }

    document.getElementById("results")?.scrollIntoView({
      behavior: "smooth",
    });
  };
  const handleSubmitRequest = (event) => {
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

  const newRequest = {
    id: Date.now(),
    resourceName: selectedResource.name,
    name: requestData.name.trim(),
    phone: requestData.phone.trim(),
    reason: requestData.reason.trim(),
    contactMethod: requestData.contactMethod,
    status: "Requested",
    date: new Date().toLocaleDateString(),
  };

 const updatedRequests = [...myRequests, newRequest];

setMyRequests(updatedRequests);

localStorage.setItem(
  "myRequests",
  JSON.stringify(updatedRequests)
);

  setRequestSubmitted(true);
};

  return (
    <div className="app">

      {/* Navbar */}
      <header className="navbar">
        <div className="logo">
          Community Assistance Navigator
        </div>

        <nav>
          <a href="#">Home</a>
          <a href="#results">Find Help</a>
          <a href="#my-requests">My Requests</a>

          <button className="login-btn">
            Login
          </button>
        </nav>
      </header>

      {/* Hero */}
      <main className="hero">

        <p className="tagline">
          Find the right help. Follow it through.
        </p>

        <h1>
          What do you need help with?
        </h1>

        <p className="subtitle">
          Tell us what you need and we'll help you find
          suitable community resources.
        </p>

        {/* Categories */}
        <div className="categories">
          {categories.map((category) => (
            <button
              key={category}
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

        {/* Selected category */}
        {selectedCategory && (
          <div className="selected-message">
            You selected:{" "}
            <strong>{selectedCategory}</strong>
          </div>
        )}

        <button
          className="find-help-btn"
          onClick={handleFindHelp}
        >
          Find Help →
        </button>

      </main>

      {/* Results */}
      {selectedCategory && (
        <section className="results-section" id="results">

          <p className="results-label">
            AVAILABLE RESOURCES
          </p>

          <h2>
            Help available for {selectedCategory}
          </h2>

          <p className="results-subtitle">
            Explore resources that may help with your needs.
          </p>

          <div className="resource-grid">

            {resources[selectedCategory].map((resource) => (
              <div
                className="resource-card"
                key={resource.name}
              >

                <span className="resource-type">
                  {resource.type}
                </span>

                <h3>
                  {resource.name}
                </h3>

                <p>
                  {resource.description}
                </p>

                <button 
                  className="details-btn"
    onClick={() => {
  setSelectedResource(resource);

  setTimeout(() => {
    document.querySelector(".resource-details")?.scrollIntoView({
      behavior: "smooth",
      block: "start",
    });
  }, 100);
}}>
          
                  View Details →
                </button>

              </div>
            ))}

          </div>

        </section>
      )}

      {/* Footer */}
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

            <span className="resource-type">
              {selectedResource.type}
            </span>

            <h2>
              {selectedResource.name}
            </h2>

            <p className="details-description">
              {selectedResource.description}
            </p>

            <div className="details-grid">

              <div className="detail-box">
                <h4>Services</h4>
                <p>
                  Community support and assistance related to this resource.
                </p>
              </div>

              <div className="detail-box">
                <h4>Eligibility</h4>
                <p>
                  Eligibility may depend on your individual needs and
                  program requirements.
                </p>
              </div>

              <div className="detail-box">
                <h4>Availability</h4>
                <p>
                  Availability depends on the selected assistance program.
                </p>
              </div>

              <div className="detail-box">
                <h4>Reliability</h4>
                <p>
                  Resource information can be verified before requesting
                  assistance.
                </p>
              </div>

              <div className="detail-box">
                <h4>Contact</h4>
                <p>
                  Contact information will be available when connected
                  with the backend.
                </p>
              </div>

            </div>

            <button 
            className="request-btn"
            onClick={()=>setShowRequestForm(true)}
            >
              Request Assistance →
            </button>

          </div>

        </section>
      )}
            {/* Request Assistance Form */}
      {showRequestForm && !requestSubmitted&& (
        <section className="request-section">

          <button
            className="back-btn"
            onClick={() => setShowRequestForm(false)}
          >
            ← Back to Resource
          </button>

          <div className="request-container">

            <p className="results-label">
              REQUEST ASSISTANCE
            </p>

            <h2>
              Request Help
            </h2>

            <p className="request-subtitle">
              Submit a request for{" "}
              <strong>{selectedResource?.name}</strong>.
            </p>

            <form
  className="request-form"
  onSubmit={handleSubmitRequest}
>

              <label>
                Full Name
              </label>

              <input
  type="text"
  placeholder="Enter your full name"
  value={requestData.name}
  onChange={(e) =>
    setRequestData({ ...requestData, name: e.target.value })
  }
  required
/>
              <label>
                Phone Number
              </label>
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
              <label>
                Reason for Assistance
              </label>
<textarea
  placeholder="Briefly explain what help you need..."
  rows="4"
  value={requestData.reason}
  onChange={(e) =>
    setRequestData({ ...requestData, reason: e.target.value })
  }
  required
></textarea>
              <label>
                Preferred Contact Method
              </label>

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

<button
  type="submit"
  disabled={requestSubmitted}
  className="submit-request-btn"
>
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
              Your request for{" "}
              <strong>{selectedResource?.name}</strong> has been recorded.
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
    onChange={(event) => {
      const updatedRequests = myRequests.map((item) =>
        item.id === request.id
          ? { ...item, status: event.target.value }
          : item
      );

      setMyRequests(updatedRequests);

      localStorage.setItem(
        "myRequests",
        JSON.stringify(updatedRequests)
      );
    }}
  >
    <option value="Requested">Requested</option>
    <option value="Contacted">Contacted</option>
    <option value="Assistance Received">Assistance Received</option>
    <option value="Unable to Assist">Unable to Assist</option>
  </select>
</label>
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
            <p>Thank you for your feedback!</p>
          ) : (
            <form
              onSubmit={(event) => {
                event.preventDefault();
                setFeedbackSubmitted(true);
              }}
            >
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

        <p>
          Find the right help. Follow it through. Learn what works.
        </p>
      </footer>

    </div>
  );
}

export default App;
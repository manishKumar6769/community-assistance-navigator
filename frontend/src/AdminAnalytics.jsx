
import { useEffect, useState } from "react";
import { getAnalytics } from "./api";
import "./AdminAnalytics.css";

function AdminAnalytics() {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    getAnalytics()
      .then(setAnalytics)
      .catch((err) => setError(err.message || "Could not load analytics."))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <main className="analytics-page"><p>Loading analytics...</p></main>;
  }

  if (error) {
    return (
      <main className="analytics-page">
        <h1>Admin Analytics</h1>
        <p className="analytics-error">{error}</p>
        <button onClick={() => window.location.reload()}>Try Again</button>
      </main>
    );
  }

  const cards = [
    ["Total Referrals", analytics.totalReferrals],
    ["Requested", analytics.requested],
    ["Contacted", analytics.contacted],
    ["Assistance Received", analytics.received],
    ["Not Received", analytics.notReceived],
    ["Success Rate", `${analytics.successRatePercent}%`],
  ];

  return (
    <main className="analytics-page">
      <p className="analytics-eyebrow">COMMUNITY ASSISTANCE NAVIGATOR</p>
      <h1>Admin Analytics</h1>
      <p className="analytics-subtitle">
        Referral outcomes and assistance statistics
      </p>

      <section className="analytics-grid">
        {cards.map(([label, value]) => (
          <article className="analytics-card" key={label}>
            <p>{label}</p>
            <h2>{value ?? 0}</h2>
          </article>
        ))}
      </section>

      <section className="analytics-panel">
        <h2>Referral Failure Reasons</h2>
        {Object.keys(analytics.failureReasons || {}).length === 0 ? (
          <p>No failure reasons recorded.</p>
        ) : (
          <ul>
            {Object.entries(analytics.failureReasons).map(([reason, count]) => (
              <li key={reason}>
                <span>{reason.replaceAll("_", " ")}</span>
                <strong>{count}</strong>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}

export default AdminAnalytics;

import { FormEvent, useState } from "react";
import { useAdminAuth } from "../lib/AdminAuthContext";

export function LoginPage() {
  const { signIn, error, loading, firebaseUser, isAdmin, signOut } = useAdminAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await signIn(email, password);
    } finally {
      setSubmitting(false);
    }
  };

  // Signed in but not an admin — this account exists (maybe a student/teacher) but
  // shouldn't be here. Show a clear message rather than a confusing blank dashboard.
  if (!loading && firebaseUser && !isAdmin) {
    return (
      <div className="login-shell">
        <div className="login-card">
          <h2>Not an admin account</h2>
          <p style={{ color: "var(--ink-soft)", fontSize: "0.88rem" }}>
            {firebaseUser.email} is signed in but doesn't have admin access on Maarifa 2026.
          </p>
          <button className="btn-primary" onClick={signOut}>Sign out</button>
        </div>
      </div>
    );
  }

  return (
    <div className="login-shell">
      <form className="login-card" onSubmit={handleSubmit}>
        <div className="sidebar-wordmark" style={{ color: "var(--forest)" }}>
          <span>Maarifa</span>
          <span style={{ color: "var(--ink-soft)" }}>Admin console</span>
        </div>
        <input type="email" placeholder="Admin email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        {error && <p className="error-text">{error}</p>}
        <button type="submit" className="btn-primary" disabled={submitting}>
          {submitting ? "Signing in…" : "Sign in"}
        </button>
        <p style={{ fontSize: "0.78rem", color: "var(--ink-soft)" }}>
          Admin accounts are created directly in Firestore (users/{"{uid}"}.role = "ADMIN") — there's no self-signup here.
        </p>
      </form>
    </div>
  );
}

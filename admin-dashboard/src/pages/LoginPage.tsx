import { FormEvent, useState } from "react";
import { useAdminAuth } from "../lib/AdminAuthContext";

export function LoginPage() {
  const { signIn, error, loading, firebaseUser, isAdmin, signOut } = useAdminAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (submitting) return;

    setSubmitting(true);
    try {
      await signIn(email.trim(), password);
    } catch {
      // Errors zote zinashughulikiwa na AdminAuthContext
    } finally {
      setSubmitting(false);
    }
  };

  // 1. Inapoangalia Hali ya Auth (Initializing state)
  if (loading && !firebaseUser) {
    return (
      <div className="login-shell">
        <div className="login-card" style={{ textAlign: "center" }}>
          <p style={{ color: "var(--ink-soft)" }}>Checking authentication...</p>
        </div>
      </div>
    );
  }

  // 2. User yupo signed in lakini hana idhini ya Admin
  if (!loading && firebaseUser && !isAdmin) {
    return (
      <div className="login-shell">
        <div className="login-card">
          <h2>Not an admin account</h2>
          <p style={{ color: "var(--ink-soft)", fontSize: "0.88rem" }}>
            {firebaseUser.email || "This account"} is signed in but doesn't have admin access on Maarifa 2026.
          </p>
          <button type="button" className="btn-primary" onClick={signOut}>
            Sign out
          </button>
        </div>
      </div>
    );
  }

  // 3. Fomu ya Kuingia (Login Form)
  return (
    <div className="login-shell">
      <form className="login-card" onSubmit={handleSubmit}>
        <div className="sidebar-wordmark" style={{ color: "var(--forest)" }}>
          <span>Maarifa</span>
          <span style={{ color: "var(--ink-soft)" }}>Admin console</span>
        </div>

        <div className="form-group" style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
          <input
            type="email"
            placeholder="Admin email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="username"
            required
            disabled={submitting}
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
            disabled={submitting}
          />
        </div>

        {error && (
          <p className="error-text" style={{ color: "var(--danger, #e53e3e)", fontSize: "0.82rem" }}>
            {error}
          </p>
        )}

        <button type="submit" className="btn-primary" disabled={submitting || loading}>
          {submitting ? "Signing in…" : "Sign in"}
        </button>

        <p style={{ fontSize: "0.78rem", color: "var(--ink-soft)" }}>
          Admin accounts are created directly in Firestore (users/{"{uid}"}.role = "ADMIN") — there's no self-signup here.
        </p>
      </form>
    </div>
  );
}

// Export default ili iendane na import yoyote ndani ya App.tsx
export default LoginPage;

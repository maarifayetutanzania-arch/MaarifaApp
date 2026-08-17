import { NavLink } from "react-router-dom";
import { collection, query, where } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { useAdminAuth } from "../lib/AdminAuthContext";
import { Teacher, Material, Payout } from "../types";

export function Sidebar() {
  const { signOut, adminProfile } = useAdminAuth();

  const { data: pendingTeachers } = useCollection<Teacher>(
    query(collection(db, "teachers"), where("verificationStatus", "==", "PENDING"))
  );
  const { data: pendingMaterials } = useCollection<Material>(
    query(collection(db, "materials"), where("status", "==", "PENDING_REVIEW"))
  );
  const { data: pendingPayouts } = useCollection<Payout>(
    query(collection(db, "payouts"), where("status", "==", "GENERATED"))
  );

  const links = [
    { to: "/", label: "Overview", badge: 0 },
    { to: "/teachers", label: "Teachers", badge: pendingTeachers.length },
    { to: "/content", label: "Content", badge: pendingMaterials.length },
    { to: "/subscriptions", label: "Subscriptions", badge: 0 },
    { to: "/payouts", label: "Payouts", badge: pendingPayouts.length },
  ];

  return (
    <aside className="sidebar">
      <div className="sidebar-wordmark">
        <span>Maarifa</span>
        <span>Admin console</span>
      </div>

      {links.map((link) => (
        <NavLink key={link.to} to={link.to} end={link.to === "/"} className={({ isActive }) => "nav-link" + (isActive ? " active" : "")}>
          <span>{link.label}</span>
          {link.badge > 0 && <span className="nav-badge">{link.badge}</span>}
        </NavLink>
      ))}

      <div style={{ flex: 1 }} />

      <div style={{ padding: "0 12px", fontSize: "0.78rem", color: "rgba(255,255,255,0.55)", marginBottom: 8 }}>
        {adminProfile?.fullName || adminProfile?.email}
      </div>
      <button onClick={signOut} className="btn-ghost" style={{ borderColor: "rgba(255,255,255,0.2)", color: "rgba(255,255,255,0.8)" }}>
        Sign out
      </button>
    </aside>
  );
}

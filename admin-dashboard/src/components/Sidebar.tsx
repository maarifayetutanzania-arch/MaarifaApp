import { useMemo } from "react";
import { NavLink } from "react-router-dom";
import { collection, query, where } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { useAdminAuth } from "../lib/AdminAuthContext";
import { Teacher, Material, Payout } from "../types";

export function Sidebar() {
  const { signOut, adminProfile } = useAdminAuth();

  // 1. Memoize queries ili kuzuia kuunda object mpya kila render
  const pendingTeachersQuery = useMemo(
    () => query(collection(db, "teachers"), where("verificationStatus", "==", "PENDING")),
    []
  );
  const pendingMaterialsQuery = useMemo(
    () => query(collection(db, "materials"), where("status", "==", "PENDING_REVIEW")),
    []
  );
  const pendingPayoutsQuery = useMemo(
    () => query(collection(db, "payouts"), where("status", "==", "GENERATED")),
    []
  );

  const { data: pendingTeachers } = useCollection<Teacher>(pendingTeachersQuery);
  const { data: pendingMaterials } = useCollection<Material>(pendingMaterialsQuery);
  const { data: pendingPayouts } = useCollection<Payout>(pendingPayoutsQuery);

  // 2. Tumia optional chaining au fallback (?? []) kuzuia crash wakati data ina-load
  const teacherBadgeCount = pendingTeachers?.length ?? 0;
  const materialBadgeCount = pendingMaterials?.length ?? 0;
  const payoutBadgeCount = pendingPayouts?.length ?? 0;

  const links = [
    { to: "/", label: "Overview", badge: 0 },
    { to: "/teachers", label: "Teachers", badge: teacherBadgeCount },
    { to: "/content", label: "Content", badge: materialBadgeCount },
    { to: "/subscriptions", label: "Subscriptions", badge: 0 },
    { to: "/payouts", label: "Payouts", badge: payoutBadgeCount },
  ];

  return (
    <aside className="sidebar">
      <div className="sidebar-wordmark">
        <span>Maarifa</span>
        <span>Admin console</span>
      </div>

      {links.map((link) => (
        <NavLink
          key={link.to}
          to={link.to}
          end={link.to === "/"}
          className={({ isActive }) => "nav-link" + (isActive ? " active" : "")}
        >
          <span>{link.label}</span>
          {link.badge > 0 && <span className="nav-badge">{link.badge}</span>}
        </NavLink>
      ))}

      <div style={{ flex: 1 }} />

      <div
        style={{
          padding: "0 12px",
          fontSize: "0.78rem",
          color: "rgba(255,255,255,0.55)",
          marginBottom: 8,
        }}
      >
        {adminProfile?.fullName || adminProfile?.email}
      </div>

      <button
        type="button"
        onClick={signOut}
        className="btn-ghost"
        style={{ borderColor: "rgba(255,255,255,0.2)", color: "rgba(255,255,255,0.8)" }}
      >
        Sign out
      </button>
    </aside>
  );
}

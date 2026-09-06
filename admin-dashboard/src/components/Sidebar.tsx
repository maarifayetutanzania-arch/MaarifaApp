import { useMemo } from "react";
import { NavLink } from "react-router-dom";
import { collection, query, where } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { useAdminAuth } from "../lib/AdminAuthContext";
import { Teacher, Material, Payout } from "../types";

export function Sidebar() {
  const { signOut, adminProfile } = useAdminAuth();

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

  const displayName = adminProfile?.fullName || adminProfile?.email || "Admin User";

  return (
    <aside className="sidebar">
      <div className="sidebar-wordmark">
        <span>Maarifa</span>
        <span>Admin Console</span>
      </div>

      <nav style={{ display: "flex", flexDirection: "column", flex: 1 }}>
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end={link.to === "/"}
            className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
          >
            <span>{link.label}</span>
            {link.badge > 0 && <span className="nav-badge">{link.badge}</span>}
          </NavLink>
        ))}
      </nav>

      <div style={{ marginTop: "auto", paddingTop: "16px", borderTop: "1px solid rgba(255,255,255,0.1)" }}>
        <div style={{ fontSize: "0.8rem", color: "#eef4ef", marginBottom: "8px" }}>
          {displayName}
        </div>
        <button type="button" onClick={signOut} className="btn-ghost" style={{ width: "100%", color: "#a7b3ac", borderColor: "rgba(255,255,255,0.2)" }}>
          Sign out
        </button>
      </div>
    </aside>
  );
}

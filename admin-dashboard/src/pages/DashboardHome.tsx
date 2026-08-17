import { collection, query, where } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatCard } from "../components/Common";
import { AppUser, Material, Subscription, Teacher } from "../types";

export function DashboardHome() {
  const { data: students, loading: l1 } = useCollection<AppUser>(query(collection(db, "users"), where("role", "==", "STUDENT")));
  const { data: teachers, loading: l2 } = useCollection<Teacher>(collection(db, "teachers"));
  const { data: activeSubs, loading: l3 } = useCollection<Subscription>(query(collection(db, "subscriptions"), where("status", "==", "ACTIVE")));
  const { data: approvedMaterials, loading: l4 } = useCollection<Material>(query(collection(db, "materials"), where("status", "==", "APPROVED")));
  const { data: pendingTeachers } = useCollection<Teacher>(query(collection(db, "teachers"), where("verificationStatus", "==", "PENDING")));
  const { data: pendingMaterials } = useCollection<Material>(query(collection(db, "materials"), where("status", "==", "PENDING_REVIEW")));

  const loading = l1 || l2 || l3 || l4;
  const revenueTzs = activeSubs.reduce((sum, s) => sum + (s.amountTzs || 0), 0);

  return (
    <div className="main">
      <div className="page-header">
        <div>
          <h1>Overview</h1>
          <p>Live snapshot across students, teachers, content and revenue.</p>
        </div>
      </div>

      <div className="stat-grid">
        <StatCard label="Registered students" value={loading ? "—" : students.length} />
        <StatCard label="Teachers" value={loading ? "—" : teachers.length} />
        <StatCard label="Active subscribers" value={loading ? "—" : activeSubs.length} />
        <StatCard label="Approved materials" value={loading ? "—" : approvedMaterials.length} />
        <StatCard label="Active-plan revenue" value={loading ? "—" : `${revenueTzs.toLocaleString()} TZS`} />
        <StatCard label="Pending teacher reviews" value={pendingTeachers.length} />
        <StatCard label="Pending content reviews" value={pendingMaterials.length} />
      </div>

      {(pendingTeachers.length > 0 || pendingMaterials.length > 0) && (
        <div className="card" style={{ borderColor: "var(--gold)" }}>
          <strong>Needs your attention</strong>
          <p style={{ color: "var(--ink-soft)", fontSize: "0.88rem", margin: "6px 0 0" }}>
            {pendingTeachers.length > 0 && `${pendingTeachers.length} teacher application(s) waiting for review. `}
            {pendingMaterials.length > 0 && `${pendingMaterials.length} material(s) waiting for moderation.`}
          </p>
        </div>
      )}
    </div>
  );
}

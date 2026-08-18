import { collection, query, where } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatCard } from "../components/Common";
import { AppUser, Material, Subscription, Teacher } from "../types";

// 1. Hamisha Queries zote nje ya component kuzuia infinite loops
const studentsQuery = query(collection(db, "users"), where("role", "==", "STUDENT"));
const teachersCollection = collection(db, "teachers");
const activeSubsQuery = query(collection(db, "subscriptions"), where("status", "==", "ACTIVE"));
const approvedMaterialsQuery = query(collection(db, "materials"), where("status", "==", "APPROVED"));
const pendingTeachersQuery = query(collection(db, "teachers"), where("verificationStatus", "==", "PENDING"));
const pendingMaterialsQuery = query(collection(db, "materials"), where("status", "==", "PENDING_REVIEW"));

export function DashboardHome() {
  // 2. Weka default values ([]) kuzuia crash wakati wa loading
  const { data: students = [], loading: l1 } = useCollection<AppUser>(studentsQuery);
  const { data: teachers = [], loading: l2 } = useCollection<Teacher>(teachersCollection);
  const { data: activeSubs = [], loading: l3 } = useCollection<Subscription>(activeSubsQuery);
  const { data: approvedMaterials = [], loading: l4 } = useCollection<Material>(approvedMaterialsQuery);
  const { data: pendingTeachers = [], loading: l5 } = useCollection<Teacher>(pendingTeachersQuery);
  const { data: pendingMaterials = [], loading: l6 } = useCollection<Material>(pendingMaterialsQuery);

  const loading = l1 || l2 || l3 || l4 || l5 || l6;

  // Safe checks kuzuia map errors
  const safeStudents = Array.isArray(students) ? students : [];
  const safeTeachers = Array.isArray(teachers) ? teachers : [];
  const safeActiveSubs = Array.isArray(activeSubs) ? activeSubs : [];
  const safeApprovedMaterials = Array.isArray(approvedMaterials) ? approvedMaterials : [];
  const safePendingTeachers = Array.isArray(pendingTeachers) ? pendingTeachers : [];
  const safePendingMaterials = Array.isArray(pendingMaterials) ? pendingMaterials : [];

  const revenueTzs = safeActiveSubs.reduce((sum, s) => sum + (s?.amountTzs || 0), 0);

  return (
    <div className="main">
      <div className="page-header">
        <div>
          <h1>Overview</h1>
          <p>Live snapshot across students, teachers, content and revenue.</p>
        </div>
      </div>

      <div className="stat-grid">
        <StatCard label="Registered students" value={loading ? "—" : safeStudents.length} />
        <StatCard label="Teachers" value={loading ? "—" : safeTeachers.length} />
        <StatCard label="Active subscribers" value={loading ? "—" : safeActiveSubs.length} />
        <StatCard label="Approved materials" value={loading ? "—" : safeApprovedMaterials.length} />
        <StatCard label="Active-plan revenue" value={loading ? "—" : `${revenueTzs.toLocaleString()} TZS`} />
        <StatCard label="Pending teacher reviews" value={loading ? "—" : safePendingTeachers.length} />
        <StatCard label="Pending content reviews" value={loading ? "—" : safePendingMaterials.length} />
      </div>

      {!loading && (safePendingTeachers.length > 0 || safePendingMaterials.length > 0) && (
        <div className="card" style={{ borderColor: "var(--gold)" }}>
          <strong>Needs your attention</strong>
          <p style={{ color: "var(--ink-soft)", fontSize: "0.88rem", margin: "6px 0 0" }}>
            {safePendingTeachers.length > 0 && `${safePendingTeachers.length} teacher application(s) waiting for review. `}
            {safePendingMaterials.length > 0 && `${safePendingMaterials.length} material(s) waiting for moderation.`}
          </p>
        </div>
      )}
    </div>
  );
}

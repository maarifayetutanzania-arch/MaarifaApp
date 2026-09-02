import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { onAuthStateChanged, signInWithEmailAndPassword, signOut as fbSignOut, User } from "firebase/auth";
import { doc, getDoc } from "firebase/firestore";
import { auth, db } from "../firebase";
import { AppUser } from "../types";

interface AdminAuthState {
  loading: boolean;
  firebaseUser: User | null;
  adminProfile: AppUser | null;
  isAdmin: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  error: string | null;
}

const AdminAuthContext = createContext<AdminAuthState | undefined>(undefined);

export function AdminAuthProvider({ children }: { children: ReactNode }) {
  const [loading, setLoading] = useState(true);
  const [firebaseUser, setFirebaseUser] = useState<User | null>(null);
  const [adminProfile, setAdminProfile] = useState<AppUser | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      setLoading(true);
      setFirebaseUser(user);

      if (!user) {
        setAdminProfile(null);
        setLoading(false);
        return;
      }

      try {
        const snap = await getDoc(doc(db, "users", user.uid));
        
        if (snap.exists()) {
          const data = snap.data() as AppUser;
          // Inakagua bila kujali kama ni "ADMIN", "admin", au "Admin"
          const userRole = String(data.role || (data as any).roleEnum || "").toUpperCase();

          if (userRole === "ADMIN") {
            setAdminProfile(data);
            setError(null);
          } else {
            setAdminProfile(null);
            setError(`Akaunti ya ${user.email} haina idhini ya ADMIN (Role iliyopo: "${data.role || 'Haina role'}").`);
          }
        } else {
          setAdminProfile(null);
          setError(`Document ya mtumiaji haijapatikana kwenye Firestore (users/${user.uid}). Create document hii kwanza.`);
        }
      } catch (err: any) {
        console.error("Error fetching admin profile:", err);
        setAdminProfile(null);
        setError("Hitilafu ya kusoma Firestore: " + (err.message || "Permission denied"));
      } finally {
        setLoading(false);
      }
    });

    return unsubscribe;
  }, []);

  const signIn = async (email: string, password: string) => {
    setError(null);
    try {
      await signInWithEmailAndPassword(auth, email, password);
    } catch (e: any) {
      let customMessage = "Kuingia imeshindikana. Angalia taarifa zako.";
      if (e.code === "auth/invalid-credential" || e.code === "auth/user-not-found" || e.code === "auth/wrong-password") {
        customMessage = "Barua pepe au nenosiri si sahihi.";
      } else if (e.code === "auth/too-many-requests") {
        customMessage = "Akaunti imefungiwa kwa muda kutokana na kujaribu mara nyingi. Jaribu tena baadae.";
      }
      
      setError(customMessage);
      throw e;
    }
  };

  const signOut = async () => {
    await fbSignOut(auth);
    setAdminProfile(null);
    setError(null);
  };

  const isAdmin = adminProfile !== null && String(adminProfile.role).toUpperCase() === "ADMIN";

  return (
    <AdminAuthContext.Provider
      value={{
        loading,
        firebaseUser,
        adminProfile,
        isAdmin,
        signIn,
        signOut,
        error
      }}
    >
      {children}
    </AdminAuthContext.Provider>
  );
}

export function useAdminAuth(): AdminAuthState {
  const ctx = useContext(AdminAuthContext);
  if (!ctx) throw new Error("useAdminAuth must be used inside AdminAuthProvider");
  return ctx;
}

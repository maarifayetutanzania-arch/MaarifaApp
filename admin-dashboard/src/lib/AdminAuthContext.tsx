import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { onAuthStateChanged, signInWithEmailAndPassword, signOut as fbSignOut, User } from "firebase/auth";
import { doc, getDoc } from "firebase/firestore";
import { auth, db } from "../firebase";
import { AppUser } from "../types";

interface AdminAuthState {
  loading: boolean;
  firebaseUser: User | null;
  adminProfile: AppUser | null;
  /** True once we've confirmed role === "ADMIN" on the Firestore user doc — this is a
   * UX guard only. The real security boundary is server-side: every admin Cloud
   * Function re-checks role itself, and Firestore rules block direct writes outright. */
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
      setFirebaseUser(user);
      if (!user) {
        setAdminProfile(null);
        setLoading(false);
        return;
      }
      const snap = await getDoc(doc(db, "users", user.uid));
      const data = snap.exists() ? (snap.data() as AppUser) : null;
      setAdminProfile(data);
      setLoading(false);
    });
    return unsubscribe;
  }, []);

  const signIn = async (email: string, password: string) => {
    setError(null);
    try {
      await signInWithEmailAndPassword(auth, email, password);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Sign-in failed");
      throw e;
    }
  };

  const signOut = async () => {
    await fbSignOut(auth);
  };

  const isAdmin = adminProfile?.role === "ADMIN";

  return (
    <AdminAuthContext.Provider value={{ loading, firebaseUser, adminProfile, isAdmin, signIn, signOut, error }}>
      {children}
    </AdminAuthContext.Provider>
  );
}

export function useAdminAuth(): AdminAuthState {
  const ctx = useContext(AdminAuthContext);
  if (!ctx) throw new Error("useAdminAuth must be used inside AdminAuthProvider");
  return ctx;
}

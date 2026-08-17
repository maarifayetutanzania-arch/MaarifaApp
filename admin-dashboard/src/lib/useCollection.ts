import { useEffect, useState } from "react";
import { Query, onSnapshot } from "firebase/firestore";

export function useCollection<T>(query: Query | null): { data: T[]; loading: boolean; error: string | null } {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!query) return;
    setLoading(true);
    const unsubscribe = onSnapshot(
      query,
      (snap) => {
        setData(snap.docs.map((d) => ({ ...(d.data() as T) })));
        setLoading(false);
      },
      (err) => {
        setError(err.message);
        setLoading(false);
      }
    );
    return unsubscribe;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  return { data, loading, error };
}

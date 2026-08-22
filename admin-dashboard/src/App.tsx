import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AdminAuthProvider } from "./lib/AdminAuthContext";
import { AdminLayout } from "./components/AdminLayout";
import LoginPage from "./pages/LoginPage";
import DashboardHome from "./pages/DashboardHome";
import TeachersPage from "./pages/TeachersPage";
import ContentPage from "./pages/ContentPage";
import SubscriptionsPage from "./pages/SubscriptionsPage";
import PayoutsPage from "./pages/PayoutsPage";

export default function App() {
  return (
    <AdminAuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<AdminLayout />}>
            <Route path="/" element={<DashboardHome />} />
            <Route path="/teachers" element={<TeachersPage />} />
            <Route path="/content" element={<ContentPage />} />
            <Route path="/subscriptions" element={<SubscriptionsPage />} />
            <Route path="/payouts" element={<PayoutsPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AdminAuthProvider>
  );
}

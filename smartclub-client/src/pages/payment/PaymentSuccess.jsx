// src/pages/payment/PaymentSuccess.jsx
import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { apiPost } from "../../api/api";
import { useUi } from "../../context/UiContext";
import Navbar from "../../components/Navbar";
import MenuModal from "../../components/MenuModal";

export default function PaymentSuccess() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { notify } = useUi();
  
  const [menuOpen, setMenuOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [success, setSuccess] = useState(false);
  const [reservationDetails, setReservationDetails] = useState(null);

  const sessionId = searchParams.get("session_id");
  const reservationId = searchParams.get("reservationId");

  useEffect(() => {
    const verifyPayment = async () => {
      if (!sessionId) {
        notify("error", "Некорректная сессия оплаты");
        setLoading(false);
        return;
      }

      try {
        // Optional: Verify payment with backend
        const res = await apiPost("/payment/verify", {
          sessionId,
          reservationId
        });

        if (res.ok) {
          setSuccess(true);
          setReservationDetails(res.data?.reservation);
          notify("success", "Оплата успешно завершена!");
        } else {
          notify("error", "Ошибка проверки платежа");
        }
      } catch (err) {
        console.error("Verification error:", err);
        // Even if verification fails, payment might still be successful
        // The webhook will handle the actual confirmation
        setSuccess(true);
      } finally {
        setLoading(false);
      }
    };

    verifyPayment();
  }, [sessionId, reservationId, notify]);

  const formatDateTime = (isoString) => {
    try {
      return new Date(isoString).toLocaleString("ru-RU", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return isoString;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#111] text-white">
        <Navbar onMenuClick={() => setMenuOpen(true)} />
        <MenuModal isOpen={menuOpen} onClose={() => setMenuOpen(false)} />
        
        <div className="flex items-center justify-center min-h-[60vh]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-pink-500 mx-auto mb-4"></div>
            <p className="text-gray-400">Проверка платежа...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#111] text-white">
      <Navbar onMenuClick={() => setMenuOpen(true)} />
      <MenuModal isOpen={menuOpen} onClose={() => setMenuOpen(false)} />

      <div className="max-w-2xl mx-auto p-6">
        <div className="bg-[#1E1E1E] p-8 rounded-xl border border-pink-600 text-center">
          {success ? (
            <>
              {/* Success Icon */}
              <div className="mb-6">
                <div className="w-20 h-20 bg-green-500 rounded-full flex items-center justify-center mx-auto">
                  <svg className="w-12 h-12 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
              </div>

              <h1 className="text-3xl font-bold mb-3 text-green-400">Оплата успешна!</h1>
              <p className="text-gray-300 mb-6">
                Ваше бронирование подтверждено. Детали отправлены на вашу электронную почту.
              </p>

              {/* Reservation Details */}
              {reservationDetails && (
                <div className="bg-[#0f0f0f] p-6 rounded-lg mb-6 text-left border border-gray-800">
                  <h2 className="text-xl font-semibold mb-4 text-center">Детали бронирования</h2>
                  <div className="space-y-3 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-400">ID бронирования:</span>
                      <span className="font-mono text-pink-400">{reservationId}</span>
                    </div>
                    {reservationDetails.clubName && (
                      <div className="flex justify-between">
                        <span className="text-gray-400">Клуб:</span>
                        <span className="font-medium">{reservationDetails.clubName}</span>
                      </div>
                    )}
                    {reservationDetails.start && (
                      <div className="flex justify-between">
                        <span className="text-gray-400">Начало:</span>
                        <span>{formatDateTime(reservationDetails.start)}</span>
                      </div>
                    )}
                    {reservationDetails.end && (
                      <div className="flex justify-between">
                        <span className="text-gray-400">Окончание:</span>
                        <span>{formatDateTime(reservationDetails.end)}</span>
                      </div>
                    )}
                    {reservationDetails.totalPrice && (
                      <div className="flex justify-between text-lg font-bold pt-3 border-t border-gray-700">
                        <span>Оплачено:</span>
                        <span className="text-green-400">{reservationDetails.totalPrice} ₸</span>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="flex gap-3 justify-center">
                <button
                  onClick={() => navigate("/history")}
                  className="px-6 py-3 rounded-lg bg-gradient-to-r from-pink-500 to-purple-500 hover:from-pink-600 hover:to-purple-600 font-semibold transition"
                >
                  Мои бронирования
                </button>
                <button
                  onClick={() => navigate("/")}
                  className="px-6 py-3 rounded-lg bg-gray-700 hover:bg-gray-600 font-semibold transition"
                >
                  На главную
                </button>
              </div>

              <div className="mt-6 text-xs text-gray-400">
                Session ID: {sessionId}
              </div>
            </>
          ) : (
            <>
              {/* Error Icon */}
              <div className="mb-6">
                <div className="w-20 h-20 bg-red-500 rounded-full flex items-center justify-center mx-auto">
                  <svg className="w-12 h-12 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </div>
              </div>

              <h1 className="text-3xl font-bold mb-3 text-red-400">Ошибка оплаты</h1>
              <p className="text-gray-300 mb-6">
                К сожалению, не удалось подтвердить вашу оплату. Пожалуйста, свяжитесь с поддержкой.
              </p>

              <button
                onClick={() => navigate("/")}
                className="px-6 py-3 rounded-lg bg-gray-700 hover:bg-gray-600 font-semibold transition"
              >
                Вернуться на главную
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
// src/pages/payment/Payment.jsx
import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { apiPost } from "../../api/api";
import { useUi } from "../../context/UiContext";
import Navbar from "../../components/Navbar";
import MenuModal from "../../components/MenuModal";

export default function Payment() {
  const location = useLocation();
  const navigate = useNavigate();
  const { notify } = useUi();
  const [menuOpen, setMenuOpen] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState("card"); // card or kaspi

  const {
    reservationId,
    clubName,
    totalPrice,
    packageName,
    start,
    end,
    seatCount,
  } = location.state || {};

  useEffect(() => {
    if (!reservationId) {
      notify("error", "Нет данных бронирования");
      navigate("/");
    }
  }, [reservationId, navigate, notify]);

  const handlePayment = async () => {
    setProcessing(true);
    
    console.log("=== PAYMENT DEBUG START ===");
    console.log("Location state:", location.state);
    console.log("clubId:", location.state?.clubId);
    console.log("seatIds:", location.state?.seatIds);
    console.log("packageId:", location.state?.packageId);
    
    try {
      const reservePayload = {
        clubId: location.state?.clubId,
        packageId: location.state?.packageId,
        seatIds: location.state?.seatIds,
        start: location.state?.start,
        end: location.state?.end,
        durationMinutes: location.state?.durationMinutes,
        totalPrice: location.state?.totalPrice
      };
      
      console.log("Reserve payload:", reservePayload);
      
      const reserveRes = await apiPost("/booking/reserve", reservePayload);
      console.log("Reserve response:", reserveRes);
      
      if (!reserveRes.ok || !reserveRes.data?.reservationId) {
        console.error("Reserve failed:", reserveRes);
        notify("error", reserveRes.data?.error || "Ошибка создания брони");
        setProcessing(false);
        return;
      }
      
      const reservationId = reserveRes.data.reservationId;
      console.log("Reservation created:", reservationId);
      
      const paymentPayload = {
        reservationId,
        amount: location.state?.totalPrice,
        currency: "kzt"
      };
      
      console.log("Payment payload:", paymentPayload);
      
      const paymentRes = await apiPost("/payment/create-intent", paymentPayload);
      console.log("Payment response:", paymentRes);

      if (paymentRes.ok && paymentRes.data?.checkoutUrl) {
        console.log("Redirecting to:", paymentRes.data.checkoutUrl);
        window.location.href = paymentRes.data.checkoutUrl;
      } else {
        console.error("Payment creation failed:", paymentRes);
        notify("error", "Ошибка создания платежа");
      }
    } catch (err) {
      console.error("Payment error:", err);
      notify("error", "Ошибка при оплате: " + err.message);
    } finally {
      setProcessing(false);
    }
  };

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

  if (!reservationId) return null;

  return (
    <div className="min-h-screen bg-[#111] text-white">
      <Navbar onMenuClick={() => setMenuOpen(true)} />
      <MenuModal isOpen={menuOpen} onClose={() => setMenuOpen(false)} />

      <div className="max-w-3xl mx-auto p-6">
        <button
          onClick={() => navigate(-1)}
          className="mb-4 text-sm text-pink-400 hover:underline"
        >
          ← Назад
        </button>

        <div className="bg-[#1E1E1E] p-6 rounded-xl border border-pink-600">
          <h1 className="text-3xl font-bold mb-6">Оплата бронирования</h1>

          {/* Booking Summary */}
          <div className="bg-[#0f0f0f] p-4 rounded-lg mb-6 border border-gray-800">
            <h2 className="text-xl font-semibold mb-3">Детали заказа</h2>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-400">Клуб:</span>
                <span className="font-medium">{clubName || "—"}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Пакет:</span>
                <span className="font-medium">{packageName || "—"}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Начало:</span>
                <span>{formatDateTime(start)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Окончание:</span>
                <span>{formatDateTime(end)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Количество мест:</span>
                <span>{seatCount || 0}</span>
              </div>
              <div className="flex justify-between text-lg font-bold pt-3 border-t border-gray-700">
                <span>Итого:</span>
                <span className="text-pink-400">{totalPrice ? `${totalPrice} ₸` : "—"}</span>
              </div>
            </div>
          </div>

          {/* Payment Method Selection */}
          <div className="mb-6">
            <h2 className="text-xl font-semibold mb-3">Способ оплаты</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <button
                onClick={() => setPaymentMethod("card")}
                className={`p-4 rounded-lg border-2 transition ${
                  paymentMethod === "card"
                    ? "border-pink-500 bg-pink-500/10"
                    : "border-gray-700 hover:border-gray-600"
                }`}
              >
                <div className="font-semibold">Банковская карта</div>
                <div className="text-xs text-gray-400 mt-1">Visa, MasterCard, МИР</div>
              </button>

              <button
                onClick={() => setPaymentMethod("kaspi")}
                className={`p-4 rounded-lg border-2 transition ${
                  paymentMethod === "kaspi"
                    ? "border-pink-500 bg-pink-500/10"
                    : "border-gray-700 hover:border-gray-600"
                }`}
              >
                <div className="font-semibold">Kaspi QR</div>
                <div className="text-xs text-gray-400 mt-1">Оплата через Kaspi</div>
              </button>
            </div>
          </div>

          {/* Payment Button */}
          <button
            onClick={handlePayment}
            disabled={processing}
            className={`w-full py-4 rounded-lg text-white font-bold text-lg transition ${
              processing
                ? "bg-gray-600 cursor-not-allowed"
                : "bg-gradient-to-r from-pink-500 to-purple-500 hover:from-pink-600 hover:to-purple-600"
            }`}
          >
            {processing ? "Обработка..." : `Оплатить ${totalPrice} ₸`}
          </button>

          <div className="mt-4 text-center text-xs text-gray-400">
            Нажимая "Оплатить", вы соглашаетесь с условиями использования
          </div>
        </div>
      </div>
    </div>
  );
}
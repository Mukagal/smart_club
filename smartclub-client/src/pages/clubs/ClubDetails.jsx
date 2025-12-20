// src/pages/clubs/ClubDetails.jsx
import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import Navbar from "../../components/Navbar";
import MenuModal from "../../components/MenuModal";
import PackageModal from "../../components/PackageModal";
import { apiGet } from "../../api/api.js";
import { useAuth } from "../../context/AuthContext";

// Local banner images (same folder style as in Home.jsx)
import banner1 from "../../assets/clubs/1.jpg";
import banner2 from "../../assets/clubs/2.jpg";
import banner3 from "../../assets/clubs/3.jpg";
import banner4 from "../../assets/clubs/4.jpg";
import banner5 from "../../assets/clubs/5.jpg";
import banner6 from "../../assets/clubs/1.jpg";
import banner7 from "../../assets/clubs/2.jpg";
import banner8 from "../../assets/clubs/3.jpg";
import banner9 from "../../assets/clubs/4.jpg";
import banner10 from "../../assets/clubs/5.jpg";

const BANNERS = [
  banner1,
  banner2,
  banner3,
  banner4,
  banner5,
  banner6,
  banner7,
  banner8,
  banner9,
  banner10,
];

const markerIcon = new L.Icon({
  iconUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

export default function ClubDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [club, setClub] = useState(null);
  const [loading, setLoading] = useState(true);
  const [menuOpen, setMenuOpen] = useState(false);
  const [packagesOpen, setPackagesOpen] = useState(false);

  // Load single club + attach banner
  useEffect(() => {
    let mounted = true;

    (async () => {
      setLoading(true);
      try {
        const res = await apiGet(`/clubs/${id}`);
        const data = res?.data ?? res;

        if (mounted) {
          // Choose banner deterministically based on club id
          let bannerIndex = 0;
          if (data && data.id !== undefined && data.id !== null) {
            const hash = String(data.id)
              .split("")
              .reduce((acc, ch) => acc + ch.charCodeAt(0), 0);
            bannerIndex = Math.abs(hash) % BANNERS.length;
          }

          setClub({
            ...data,
            bannerUrl: BANNERS[bannerIndex],
          });
        }
      } catch (err) {
        console.error("Failed to load club:", err);
        if (mounted) setClub(null);
      } finally {
        if (mounted) setLoading(false);
      }
    })();

    return () => {
      mounted = false;
    };
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen bg-[#05050b] text-white">
        <Navbar onMenuClick={() => setMenuOpen(true)} />
        <MenuModal isOpen={menuOpen} onClose={() => setMenuOpen(false)} />
        <div className="max-w-4xl mx-auto p-6">
          <div className="text-gray-300">Загрузка...</div>
        </div>
      </div>
    );
  }

  if (!club) {
    return (
      <div className="min-h-screen bg-[#05050b] text-white">
        <Navbar onMenuClick={() => setMenuOpen(true)} />
        <MenuModal isOpen={menuOpen} onClose={() => setMenuOpen(false)} />
        <div className="max-w-4xl mx-auto p-6">
          <button
            onClick={() => navigate(-1)}
            className="mb-4 text-sm text-pink-400"
          >
            ← Назад
          </button>
          <div className="bg-[#1E1E1E] p-6 rounded-xl">Клуб не найден</div>
        </div>
      </div>
    );
  }

  // Choose which image to show: banner first, then fallback to club.image
  const mainImage = club.bannerUrl || club.image;

  const grouped = {};
  (club.prices || []).forEach((p) => {
    const cat = p.category || "Прочее";
    if (!grouped[cat]) grouped[cat] = [];
    grouped[cat].push(p);
  });

  const handlePackageSelect = (pkg) => {
    const packageId =
      pkg.id ??
      pkg.service ??
      pkg.title ??
      (pkg.category + "-" + (pkg.service || ""));
    const url = `/booking?clubId=${encodeURIComponent(
      club.id
    )}&packageId=${encodeURIComponent(packageId)}`;
    navigate(url, { state: { selectedPackage: pkg } });
  };

  return (
    <div className="min-h-screen bg-[#05050b] text-white">
      <Navbar onMenuClick={() => setMenuOpen(true)} />
      <MenuModal isOpen={menuOpen} onClose={() => setMenuOpen(false)} />

      <PackageModal
        isOpen={packagesOpen}
        onClose={() => setPackagesOpen(false)}
        packages={club.prices || []}
        onSelect={(pkg) => {
          setPackagesOpen(false);
          handlePackageSelect(pkg);
        }}
      />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-6 sm:py-10">
        {/* Back */}
        <button
          onClick={() => navigate(-1)}
          className="mb-4 text-xs sm:text-sm text-pink-400 hover:text-pink-300 transition"
        >
          ← Назад к списку
        </button>

        {/* HERO BANNER */}
        <section className="relative overflow-hidden rounded-3xl border border-white/10 bg-gradient-to-r from-[#090818] via-[#090818] to-[#050509] shadow-2xl shadow-black/60 mb-8">
          {mainImage && (
            <img
              src={mainImage}
              alt={club.name}
              className="absolute inset-0 w-full h-full object-cover"
            />
          )}

          {/* dark gradient for readability */}
          <div className="absolute inset-0 bg-gradient-to-r from-black/85 via-black/70 to-black/20" />

          <div className="relative z-10 px-6 sm:px-10 py-6 sm:py-8 flex flex-col gap-5 sm:gap-6 md:flex-row md:items-end">
            {/* Left: text */}
            <div className="flex-1 min-w-0 space-y-3">
              <div className="inline-flex items-center gap-2 text-[11px] uppercase tracking-[0.18em] text-gray-300">
                <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 shadow shadow-emerald-400/70" />
                <span>Gaming club</span>
              </div>

              <h1 className="text-2xl sm:text-3xl md:text-4xl font-semibold leading-tight">
                {club.name}
              </h1>

              {club.address && (
                <p className="text-xs sm:text-sm text-gray-300 flex flex-wrap gap-1">
                  <span className="opacity-80">📍</span>
                  <span>{club.address}</span>
                </p>
              )}

              {club.description && (
                <p className="text-xs sm:text-sm text-gray-300 max-w-xl leading-relaxed">
                  {club.description}
                </p>
              )}

              <div className="flex flex-wrap gap-3 pt-1">
                {club.phone && (
                  <a
                    href={`tel:${club.phone.replace(/\D/g, "")}`}
                    className="text-xs sm:text-sm text-gray-200 underline underline-offset-4 decoration-gray-500 hover:text-white hover:decoration-pink-400"
                  >
                    ☎ {club.phone}
                  </a>
                )}
              </div>
            </div>

            {/* Right: actions */}
            <div className="flex flex-col items-stretch sm:items-end gap-3">
              <button
                onClick={() => {
                  if (!user) {
                    navigate("/login", {
                      state: { from: `/clubs/${club.id}` },
                    });
                    return;
                  }
                  setPackagesOpen(true);
                }}
                className="inline-flex items-center justify-center rounded-full bg-white text-black px-5 py-2.5 text-sm font-semibold shadow-lg hover:bg-gray-100 transition"
              >
                Забронировать
              </button>

              <button
                onClick={() => {
                  navigator.clipboard?.writeText(window.location.href);
                  alert("Ссылка скопирована");
                }}
                className="inline-flex items-center justify-center rounded-full border border-white/20 bg-white/5 px-4 py-2 text-xs sm:text-sm text-gray-100 hover:bg-white/10 transition"
              >
                Поделиться клубом
              </button>
            </div>
          </div>
        </section>

        {/* MAP + SHORT PRICES */}
        <section className="grid gap-6 md:grid-cols-[minmax(0,1.2fr)_minmax(0,1fr)] mb-10">
          {/* Map card */}
          <div className="rounded-2xl border border-white/10 bg-[#101018] overflow-hidden shadow-lg">
            <div className="px-4 py-3 border-b border-white/5 flex items-center justify-between">
              <h2 className="text-sm font-semibold text-gray-100">
                Расположение клуба
              </h2>
              <span className="text-[11px] text-gray-400">
                OpenStreetMap • 15x zoom
              </span>
            </div>

            <div className="h-72">
              {club.latitude && club.longitude ? (
                <MapContainer
                  center={[club.latitude, club.longitude]}
                  zoom={15}
                  style={{ height: "100%", width: "100%" }}
                  scrollWheelZoom={false}
                >
                  <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                  <Marker
                    position={[club.latitude, club.longitude]}
                    icon={markerIcon}
                  >
                    <Popup>{club.name}</Popup>
                  </Marker>
                </MapContainer>
              ) : (
                <div className="w-full h-full flex items-center justify-center text-gray-500 text-sm">
                  Локация не указана
                </div>
              )}
            </div>
          </div>

          {/* Short tariffs */}
          <div className="rounded-2xl border border-white/10 bg-[#101018] p-4 sm:p-5 shadow-lg flex flex-col">
            <h2 className="text-sm font-semibold text-gray-100 mb-3">
              Краткие тарифы
            </h2>
            <div className="flex flex-wrap gap-2 mb-4">
              {(club.prices || [])
                .slice(0, 4)
                .map((p, i) => (
                  <div
                    key={i}
                    className="px-3 py-2 bg-white/5 border border-white/10 rounded-full text-xs sm:text-sm text-gray-100"
                  >
                    {(p.service ?? p.title ?? p.category) +
                      (p.price ? ` — ${p.price}` : "")}
                  </div>
                ))}
              {(!club.prices || club.prices.length === 0) && (
                <div className="text-xs text-gray-400">
                  Информация о тарифах скоро появится.
                </div>
              )}
            </div>

            <p className="text-[11px] text-gray-400 mt-auto">
              Полный список цен и пакетов вы можете посмотреть ниже, либо
              перейти к бронированию, чтобы выбрать удобный тариф.
            </p>
          </div>
        </section>

        {/* PRICES / SERVICES TABLES */}
        <section>
          <h2 className="text-xl sm:text-2xl font-semibold mb-4">
            Цены и услуги
          </h2>

          {Object.keys(grouped).length === 0 ? (
            <div className="text-gray-400 text-sm bg-[#111018] border border-white/10 rounded-2xl px-4 py-6">
              Информация о ценах отсутствует
            </div>
          ) : (
            Object.entries(grouped).map(([category, items]) => (
              <div key={category} className="mb-5">
                <h3 className="text-base sm:text-lg font-semibold mb-2">
                  {category}
                </h3>
                <div className="bg-[#0f0f15] border border-white/10 rounded-2xl">
                  {items.map((it, i) => (
                    <div
                      key={i}
                      className="flex justify-between items-center px-4 py-3 border-b border-white/5 last:border-b-0"
                    >
                      <div className="pr-4">
                        <div className="font-medium text-sm sm:text-base">
                          {it.service || it.title || "Услуга"}
                        </div>
                        {it.unit && (
                          <div className="text-xs text-gray-400">
                            {it.unit}
                          </div>
                        )}
                      </div>
                      <div className="text-right">
                        <div className="font-semibold text-sm sm:text-base">
                          {it.price}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))
          )}
        </section>
      </div>
    </div>
  );
}

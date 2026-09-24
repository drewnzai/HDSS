import {
    AlertTriangle,
    CheckCircle2,
    CircleAlert,
    Info,
} from "lucide-react";
import { useLocation, useNavigate } from "react-router-dom";
import type { LucideIcon } from "lucide-react";
import { useEffect, useState } from "react";

export type FlashType = "success" | "warning" | "error" | "info";

export interface FlashLocationState {
    flash?: string;
    flashType?: FlashType;
}

interface FlashConfig {
    icon: LucideIcon;
}

const flashConfig: Record<FlashType, FlashConfig> = {
    success: {
        icon: CheckCircle2,
    },
    warning: {
        icon: AlertTriangle,
    },
    error: {
        icon: CircleAlert,
    },
    info: {
        icon: Info,
    },
};

function Flash() {
    const location = useLocation();
    const navigate = useNavigate();

    const state = location.state as FlashLocationState | null;

    const [message, setMessage] = useState<string | null>(
        state?.flash ?? null
    );

    const [type, setType] = useState<FlashType>(
        state?.flashType ?? "success"
    );

    useEffect(() => {
        if (!state?.flash) {
            return;
        }

        setMessage(state.flash);
        setType(state.flashType ?? "success");

        const timer = window.setTimeout(() => {
            setMessage(null);

            navigate(location.pathname, {
                replace: true,
                state: {},
            });
        }, 4000);

        return () => window.clearTimeout(timer);
    }, [
        state?.flash,
        state?.flashType,
        navigate,
        location.pathname,
    ]);

    if (!message) {
        return null;
    }

    const { icon: Icon } = flashConfig[type];

    return (
        <div
            className={`flash flash--${type}`}
            role={type === "error" ? "alert" : "status"}
            aria-live={type === "error" ? "assertive" : "polite"}
        >
            <Icon
                className="flash__icon"
                size={17}
                strokeWidth={2}
                aria-hidden="true"
            />

            <span>{message}</span>
        </div>
    );
}

export default Flash;
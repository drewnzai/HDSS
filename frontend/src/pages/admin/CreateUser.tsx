import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useCreateUserMutation } from "../../store/AdminUserApi";

const ROLE_OPTIONS = ["ADMIN", "USER"]; // adjust to match your UserRole enum exactly

interface FormState {
    firstName: string;
    lastName: string;
    email: string;
    role: string;
    password: string;
    confirmPassword: string;
}

const initialForm: FormState = {
    firstName: "",
    lastName: "",
    email: "",
    role: "USER",
    password: "",
    confirmPassword: ""
};

function CreateUser() {
    const [form, setForm] = useState<FormState>(initialForm);
    const [passwordError, setPasswordError] = useState<string | null>(null);
    const [createUser, { isLoading, error }] = useCreateUserMutation();
    const navigate = useNavigate();

    const handleChange = (field: keyof FormState) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setForm((prev) => ({ ...prev, [field]: e.target.value }));
        if (field === "password" || field === "confirmPassword") {
            setPasswordError(null);
        }
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();

        if (form.password !== form.confirmPassword) {
            setPasswordError("Passwords don't match.");
            return;
        }

        try {
            const message = await createUser({
                firstName: form.firstName,
                lastName: form.lastName,
                email: form.email,
                role: form.role,
                password: form.password
            }).unwrap();

            navigate("/admin/users", { replace: true, state: { flash: message } });
        } catch {
            // error state below reflects the failure
        }
    };

    return (
        <div className="card">
            <span className="ledger-section__eyebrow">§ Admin — Add User</span>
            <h1>Create user</h1>

            <form onSubmit={handleSubmit} noValidate>
                <div className="ledger-section">
                    <span className="ledger-section__eyebrow">§ 01 — Identity</span>
                    <div className="field-group">
                        <div className="field">
                            <label className="field__label" htmlFor="firstName">
                                First name
                            </label>
                            <input
                                id="firstName"
                                value={form.firstName}
                                onChange={handleChange("firstName")}
                                required
                            />
                        </div>

                        <div className="field">
                            <label className="field__label" htmlFor="lastName">
                                Last name
                            </label>
                            <input
                                id="lastName"
                                value={form.lastName}
                                onChange={handleChange("lastName")}
                                required
                            />
                        </div>

                        <div className="field">
                            <label className="field__label" htmlFor="email">
                                Email
                            </label>
                            <input
                                id="email"
                                type="email"
                                value={form.email}
                                onChange={handleChange("email")}
                                required
                            />
                        </div>

                        <div className="field">
                            <label className="field__label" htmlFor="role">
                                Role
                            </label>
                            <select id="role" value={form.role} onChange={handleChange("role")} required>
                                {ROLE_OPTIONS.map((role) => (
                                    <option key={role} value={role}>
                                        {role}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                </div>

                <div className="ledger-section">
                    <span className="ledger-section__eyebrow">§ 02 — Credentials</span>
                    <div className="field-group">
                        <div className={`field ${passwordError ? "field--invalid" : ""}`}>
                            <label className="field__label" htmlFor="password">
                                Password
                            </label>
                            <input
                                id="password"
                                type="password"
                                autoComplete="new-password"
                                value={form.password}
                                onChange={handleChange("password")}
                                required
                                minLength={8}
                            />
                        </div>

                        <div className={`field ${passwordError ? "field--invalid" : ""}`}>
                            <label className="field__label" htmlFor="confirmPassword">
                                Confirm password
                            </label>
                            <input
                                id="confirmPassword"
                                type="password"
                                autoComplete="new-password"
                                value={form.confirmPassword}
                                onChange={handleChange("confirmPassword")}
                                required
                            />
                            {passwordError && <span className="field__error">{passwordError}</span>}
                        </div>
                    </div>
                </div>

                {error && (
                    <p className="field__error" style={{ marginTop: "var(--space-4)" }}>
                        Couldn't create this user. Check the details and try again.
                    </p>
                )}

                <div
                    className="field-group"
                    style={{ flexDirection: "row", gap: "var(--space-3)", marginTop: "var(--space-6)" }}
                >
                    <button type="submit" className="btn btn--primary" disabled={isLoading}>
                        {isLoading ? "Creating…" : "Create user"}
                    </button>
                    <button
                        type="button"
                        className="btn btn--ghost"
                        onClick={() => navigate("/admin/users")}
                    >
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
}

export default CreateUser;
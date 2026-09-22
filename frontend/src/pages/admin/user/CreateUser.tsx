import { useState } from "react";
import type {
    ChangeEvent,
    FormEvent,
} from "react";
import { useNavigate } from "react-router-dom";
import FormPage from "../../../components/form-page/FormPage";
import { useCreateUserMutation } from "../../../redux/AdminUserApi";


const ROLE_OPTIONS = ["ADMIN", "USER"] as const;

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
    confirmPassword: "",
};

function CreateUser() {
    const navigate = useNavigate();

    const [form, setForm] = useState<FormState>(initialForm);
    const [passwordError, setPasswordError] = useState<string | null>(null);

    const [createUser, { isLoading, error }] =
        useCreateUserMutation();

    const handleChange =
        (field: keyof FormState) =>
            (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
                setForm((previous) => ({
                    ...previous,
                    [field]: event.target.value,
                }));

                if (
                    field === "password" ||
                    field === "confirmPassword"
                ) {
                    setPasswordError(null);
                }
            };

    const handleSubmit = async (
        event: FormEvent<HTMLFormElement>
    ) => {
        event.preventDefault();

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
                password: form.password,
            }).unwrap();

            navigate("/admin/users", {
                replace: true,
                state: { flash: message },
            });
        } catch {
            // RTK Query error state is displayed below.
        }
    };

    return (
        <FormPage
            eyebrow="User Management"
            title="Create user"
            description="Create a user account and assign access to the HDSS field portal."
            submitLabel="Create user"
            loadingLabel="Creating…"
            cancelLabel="Back to users"
            isLoading={isLoading}
            error={
                error
                    ? "Couldn't create this user. Check the details and try again."
                    : null
            }
            onSubmit={handleSubmit}
            onCancel={() => navigate("/admin/users")}
            sections={[
                {
                    eyebrow: "01 — Identity",
                    children: (
                        <div className="form-fields">
                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="firstName"
                                >
                                    First name
                                </label>

                                <input
                                    id="firstName"
                                    className="form-field__input"
                                    value={form.firstName}
                                    onChange={handleChange("firstName")}
                                    autoComplete="given-name"
                                    required
                                />
                            </div>

                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="lastName"
                                >
                                    Last name
                                </label>

                                <input
                                    id="lastName"
                                    className="form-field__input"
                                    value={form.lastName}
                                    onChange={handleChange("lastName")}
                                    autoComplete="family-name"
                                    required
                                />
                            </div>

                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="email"
                                >
                                    Email
                                </label>

                                <input
                                    id="email"
                                    className="form-field__input"
                                    type="email"
                                    value={form.email}
                                    onChange={handleChange("email")}
                                    autoComplete="email"
                                    required
                                />
                            </div>

                            <div className="form-field">
                                <label
                                    className="form-field__label"
                                    htmlFor="role"
                                >
                                    Role
                                </label>

                                <select
                                    id="role"
                                    className="form-field__input"
                                    value={form.role}
                                    onChange={handleChange("role")}
                                    required
                                >
                                    {ROLE_OPTIONS.map((role) => (
                                        <option key={role} value={role}>
                                            {role}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>
                    ),
                },
                {
                    eyebrow: "02 — Credentials",
                    children: (
                        <div className="form-fields">
                            <div
                                className={`form - field ${passwordError
                                    ? "form-field--invalid"
                                    : ""
                                    } `}
                            >
                                <label
                                    className="form-field__label"
                                    htmlFor="password"
                                >
                                    Password
                                </label>

                                <input
                                    id="password"
                                    className="form-field__input"
                                    type="password"
                                    autoComplete="new-password"
                                    value={form.password}
                                    onChange={handleChange("password")}
                                    minLength={8}
                                    required
                                />
                            </div>

                            <div
                                className={`form - field ${passwordError
                                    ? "form-field--invalid"
                                    : ""
                                    } `}
                            >
                                <label
                                    className="form-field__label"
                                    htmlFor="confirmPassword"
                                >
                                    Confirm password
                                </label>

                                <input
                                    id="confirmPassword"
                                    className="form-field__input"
                                    type="password"
                                    autoComplete="new-password"
                                    value={form.confirmPassword}
                                    onChange={handleChange(
                                        "confirmPassword"
                                    )}
                                    required
                                />

                                {passwordError && (
                                    <span className="form-field__error">
                                        {passwordError}
                                    </span>
                                )}
                            </div>
                        </div>
                    ),
                },
            ]}
        />
    );
}

export default CreateUser;
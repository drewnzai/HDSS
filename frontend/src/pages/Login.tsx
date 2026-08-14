import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useLoginMutation } from "../store/AuthApi";
import ThemeToggle from "../components/ThemeToggle";

function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [login, { isLoading, error }] = useLoginMutation();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await login({ username, password }).unwrap();
            navigate("/", { replace: true });
        } catch {
            // error state below already reflects the failure
        }
    };

    return (
        <div className="page">
            <header className="page__header">
                <span className="record-id">HDSS · Field Portal</span>
                <ThemeToggle />
            </header>

            <main className="page__content">
                <div className="card">
                    <h1>Sign in</h1>
                    <p>Enter your field credentials to access household records.</p>

                    <form onSubmit={handleSubmit} noValidate>
                        <div className="ledger-section">
                            <span className="ledger-section__eyebrow">§ 01 — Credentials</span>
                            <div className="field-group">
                                <div className="field">
                                    <label className="field__label" htmlFor="username">
                                        Username
                                    </label>
                                    <input
                                        id="username"
                                        autoComplete="username"
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                        required
                                    />
                                </div>

                                <div className="field">
                                    <label className="field__label" htmlFor="password">
                                        Password
                                    </label>
                                    <input
                                        id="password"
                                        type="password"
                                        autoComplete="current-password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        required
                                    />
                                </div>
                            </div>
                        </div>

                        {error && (
                            <p className="field__error" style={{ marginTop: "var(--space-4)" }}>
                                Incorrect username or password.
                            </p>
                        )}

                        <button
                            type="submit"
                            className="btn btn--primary btn--full"
                            disabled={isLoading}
                            style={{ marginTop: "var(--space-6)" }}
                        >
                            {isLoading ? "Signing in…" : "Sign in"}
                        </button>
                    </form>
                </div>
            </main>
        </div>
    );
}

export default Login;
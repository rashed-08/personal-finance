/**
 * Extracts a human-readable message from an axios error.
 *
 * The backend returns RFC-7807 ProblemDetail bodies with a "detail" field.
 */
export function errorMessage(
    err: unknown,
    fallback = "Something went wrong. Please try again.",
): string {
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;

    return detail ?? fallback;
}

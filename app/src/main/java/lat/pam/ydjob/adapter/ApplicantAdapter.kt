package lat.pam.ydjob.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import lat.pam.ydjob.ApplicantProfileActivity
import lat.pam.ydjob.R
import lat.pam.ydjob.databinding.ItemApplicantBinding
import lat.pam.ydjob.model.Application

class ApplicantAdapter(
    private val applicantList: List<Application>,
    private val context: Context
) : RecyclerView.Adapter<ApplicantAdapter.ApplicantViewHolder>() {

    inner class ApplicantViewHolder(val binding: ItemApplicantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(app: Application) {
            binding.tvApplicantName.text = app.applicantName
            binding.tvApplicantEmail.text = "Tap to view profile"
            binding.chipStatus.text = app.status

            if (!app.applicantPhoto.isNullOrEmpty()) {
                Glide.with(context)
                    .load(app.applicantPhoto)
                    .apply(
                        RequestOptions()
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                    )
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_profile_placeholder)
            }

            binding.root.setOnClickListener {
                val intent = Intent(context, ApplicantProfileActivity::class.java)
                intent.putExtra("APPLICANT_ID", app.userId)
                context.startActivity(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val binding = ItemApplicantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApplicantViewHolder(binding)
    }

    override fun getItemCount(): Int = applicantList.size

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {
        holder.bind(applicantList[position])
    }
}
